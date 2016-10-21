package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerService extends Service implements SensorEventListener {
    private final String TAG = "ServerService";
    private Webserver mWebserver;
    private int mPort = 8088;


    private volatile float mTempurature;
    private volatile float mAmbientTempurature;
    private volatile float mLight;
    private volatile float mPressure;
    private volatile float mProximity;
    private volatile float mHumidity;
    private volatile float[] mAcceleration;
    private volatile float[] mGravity;
    private volatile float[] mGyroscope;
    private volatile float[] mLinearAcceleration;
    private volatile float[] mMagnetic;
    private volatile float[] mOrientation;
    private volatile float[] mRotation;

    private HandlerThread mHandlerThread;


    // We do not need this, but per the documentation we need to return null
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandlerThread = new HandlerThread("SensorListener");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        Handler handler = new Handler(looper);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for(Sensor s : sensors) {
            // The callbacks should be executed on the handler thread this way
            if (SensorTypesImpl.getNameFromType(s.getType()).equals("nosensor")) {
                Log.d(TAG, "Will ignore this sensor: " + s.getName() + " type: " + s.getType());
            } else {
                sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL, handler);
            }

        }

        InetAddress inetAddress = (InetAddress) intent.getSerializableExtra("IPAddress");

        Log.d(TAG, "ServerService got onStartCommand, containing address: " + inetAddress.toString());


        // The Webserver has a main thread that does very little, accepts incoming connections,
        // and hands them off into Runnables that run on a ThreadPool and take longer to actually
        // handle the request
        mWebserver = new Webserver(inetAddress);

        // setting daemon flag tells the system to kill this thread if it's trying to shut down
        // the JVM, which is good for us if we forget to handle something. The JVM would only shut
        // down if our service reaches the end of it's lifecycle as well.
        mWebserver.setDaemon(true);
        mWebserver.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        try {
            // This should release the blocking accept() from the Webserver thread
            mWebserver.mServerSocket.close();
            mHandlerThread.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This sets the isInterrupted flag in the Webserver and terminates interruptible methods
        mWebserver.interrupt();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor respondingSensor = event.sensor;
        switch (respondingSensor.getType()) {
            case Sensor.TYPE_TEMPERATURE:
            case 65536:
                mTempurature = event.values[0];
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mAmbientTempurature = event.values[0];
                break;
            case Sensor.TYPE_LIGHT:
                mLight = event.values[0];
                break;
            case Sensor.TYPE_PRESSURE:
                mPressure = event.values[0];
                break;
            case Sensor.TYPE_PROXIMITY:
                mProximity = event.values[0];
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                mHumidity = event.values[0];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mAcceleration = event.values.clone();
                break;
            case Sensor.TYPE_GRAVITY:
                mGravity = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyroscope = event.values.clone();
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                mLinearAcceleration = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ORIENTATION:
                mOrientation = event.values.clone();
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                mRotation = event.values.clone();
                break;
            default:
                Log.d(TAG, "Unexpected Sensor " +  event.sensor.getName());

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Fuck off
    }

    private class Webserver extends Thread {
        public ServerSocket mServerSocket;

        private final String WSTAG = "Webserver";
        private InetAddress mInetAddress;

        Webserver(InetAddress inetAddress) {
            mInetAddress = inetAddress;
        }

        @Override
        public void run() {
            try {
                // Create the ServerSocket, the IP address will be the first among those associated
                // with the adapter called wlan0, we may have to change this
                // The standard queue size for incoming connections is 50, that should be plenty
                mServerSocket = new ServerSocket();
                mServerSocket.bind(new InetSocketAddress(mInetAddress, mPort));

                // This is the thread pool that will handle individual requests in a runnable each
                ExecutorService executor = Executors.newCachedThreadPool();

                Log.d(WSTAG, "Entering main webserver loop");
                // The main server loop that should quickly accept connections one after the other
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        // Blocking call to wait for connection attempts
                        Socket socket = mServerSocket.accept();

                        // Immediately hand the request off to the executor
                        executor.execute(new RequestHandler(socket));

                    } catch (SocketException e) {
                        Log.d(WSTAG, "Socket exception raised, perhaps due to a call to close()?");
                    }

                    // And get ready for the next connection
                }

                Log.d(WSTAG, "Exiting main webserver loop, which means we have been interrupted");

                // Ends the threads in the pool gracefully
                executor.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class RequestHandler implements Runnable {
        private final String RHTAG = "RequestHandler";
        private final String URI_PREFIX = "ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices.";
        Socket mSocket;
        private final int BUFFER_SIZE = 128;

        public RequestHandler(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            Log.d(RHTAG, "There was a request that reached the request handler from address: " + mSocket.getInetAddress());

            try {
                // Reading the request

                // The input stream provides the request data
                InputStream inputStream = mSocket.getInputStream();

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = 0;


                // You can treat a ByteArrayOutputStream as a byte array with automatic resizing
                // basically
                ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();

                // This string will hold the part of the request we have read
                String request = "";
                String requestBody = ""; // Might remain empty

                // Do a first read. The InputSteam.read function will attempt to read up to the
                // size of buffer number of bytes, but the lower bound is just one byte
                if ((bytesRead = inputStream.read(buffer)) < 1){
                    // No request at all, something is broken
                    requestBytes.close();
                    mSocket.close();
                    return;
                }

                // Shovel from the small into the dynamic buffer
                requestBytes.write(buffer, 0, bytesRead);

                // Build a string of what we have so far
                request = requestBytes.toString("UTF-8");

                // See if the end of the header is already in it otherwise repeat
                // the reading process
                while(request.indexOf("\r\n\r\n") == -1) {

                    // This should not block, because if there hasn't been a double linebreak the
                    // header is not even done, so on a new read attempt there must be at least one
                    // byte available.
                    if ((bytesRead = inputStream.read(buffer)) == -1 ){
                        // No full header, something is broken
                        requestBytes.close();
                        mSocket.close();
                        return;
                    }

                    requestBytes.write(buffer, 0, bytesRead);
                    request = requestBytes.toString("UTF-8");

                }

                // Once we reach this point we have the whole header or maybe a bit more, both as a
                // byte sequence in the requestBytes and a String in the request variables. All the
                // characters up to the double linebreak should be ASCII according to the W3C.
                // ASCII is a subset of UTF-8 and most importantly each character has one byte,
                // so we can properly calculate from where the number of bytes in the
                // Content-Length field starts counting.
                // The next step is parsing the header.

                // This hash map is for storing header fields and their contents. It might be
                // overkill, but it has a nice interface, so why not
                HashMap<String, String> headerFields = new HashMap<>(10);

                // The scanner iterates over whitespace-separated tokens and is nice to use
                // It can do a lot more, like scanning for regex but I didn't use any of that
                // It can also work on streams which I tried, but even when we use a String as
                // input it has the nice property that it doesn't make multiple passes
                Scanner scanner = new Scanner(request);

                // Parse first line
                String method = scanner.next();
                String uri = scanner.next();
                String httpVersion = scanner.next();
                scanner.nextLine();


                // Parse the rest of the header fields until the empty line, which indicates the
                // end of the header
                String line;
                String[] tokens;
                while (!(line = scanner.nextLine()).equals("")) {

                    // According to rfc2616 section4.2 the header field are followed by a colon
                    // and one or more whitespace characters.
                    tokens = line.split("[:][ ]+", 2);

                    // Same source:  The names are case-insensitive.
                    headerFields.put(tokens[0].toLowerCase(), tokens[1]);
                }

                int contentLength;

                // Now we can finally try to read the Content-Length field
                if (headerFields.containsKey("content-length")) {

                    contentLength = Integer.parseInt(headerFields.get("content-length"));
                    Log.d(TAG, "contentLength (from HashMap): " + contentLength);

                    // ASCII so this is number of characters and number of bytes
                    int headerLength = request.indexOf("\r\n\r\n") + 4;

                    int stillToRead = contentLength + headerLength - requestBytes.size();

                    // Sanity check on the request size, PHP's default is 2MB
                    if (stillToRead > 2 * 1024 * 1024) stillToRead = 2 * 1024 * 1024;

                    // Read in the rest of the request body, in the same manner as before only with
                    // an added upper bound of stillToRead
                    while (stillToRead > 0){

                        bytesRead = inputStream.read(buffer, 0, buffer.length < stillToRead ? buffer.length : stillToRead);

                        if (bytesRead == -1){
                            // Something went wrong
                            requestBytes.close();
                            mSocket.close();
                            return;
                        }

                        requestBytes.write(buffer, 0, bytesRead);
                        request = requestBytes.toString("UTF-8");
                        stillToRead = stillToRead - bytesRead;

                    }

                    requestBody = request.substring(headerLength);

                } else {

                    // If there is no such field and the connection is not closed then the
                    // assumption should be that there is no body except, if the request was
                    // chunked but I think we don't need to handle that

                }

                Log.d(TAG, "Full request read: \n" + request);

                // We can just define our URL scheme ourselves basically. Maybe first layer for
                // the sensor/actuators distinction, second layer for sensor name, third layer for
                // some sort of sub selection for example x, y, or z for acceleration? Also watch
                // out, uriParts[0] is going to be empty because before the first / is only the
                // empty String.
                String[] uriParts = uri.split("[/]");



                if (method.equals("GET")) {

                    if (uriParts.length <= 1) { //  Path = "/"
                        sendResponse(buildHTMLBody(
                                "<h2>Home VS Pascalo</h2>\n" +
                                "<p><a href=\"sensors/acceleration\">Acceleration</a></p>\n" +
                                "<p><a href=\"sensors/pressure\">Pressure</a></p>\n" +
                                "<p><a href=\"sensors/temperature\">Temperature</a></p>\n"
                        ));
                    }

                    if (uriParts[1].equals("sensors")) {

                        // Handle request and listen to event
                        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                        if(uriParts[2].equals("acceleration")) {

                            sendResponse(buildHTMLBodyWithHomeButton(
                                    "<p>x: " + String.valueOf(mAcceleration[0]) + "m/s<sup>2</sup></p>\n" +
                                    "<p>y: " + String.valueOf(mAcceleration[1]) + "m/s<sup>2</sup></p>\n" +
                                    "<p>z: " + String.valueOf(mAcceleration[2]) + "m/s<sup>2</sup></p>\n"));

                        } else if (uriParts[2].equals("temperature")) {


                            sendResponse(buildHTMLBodyWithHomeButton(
                                    "<p>" + String.valueOf(mTempurature) + SensorTypesImpl.getUnitString("temperature") + "</p>\n" +
                                    "<p>" + String.valueOf(mAmbientTempurature) + SensorTypesImpl.getUnitString("temperature") + "</p>\n"));

                        } else if (uriParts[2].equals("pressure")){

                            sendResponse(buildHTMLBodyWithHomeButton(
                                    "<p>" + String.valueOf(mPressure) + SensorTypesImpl.getUnitString("pressure") + "</p>\n"));

                        } else {
                            // Send 404 back ?
                            sendResponse(buildHTMLBodyWithHomeButton(
                                    "<h2>Resource " + uriParts[2] +" not found</h2>"));
                        }

                    } else {
                        sendResponse(buildHTMLBodyWithHomeButton(
                                "<h2>Method GET not allowed for " + uriParts[1] + "</h2>"));
                    }
                } else if( method.equals("POST") ) {

                    if (uriParts[1].equals("actuators")) {

                        // TODO: read some values from the body (requestBody) and do something with them

                        sendResponse(buildHTMLBodyWithHomeButton(
                                "<h2>Hello yes, I'm an actuator, thanks for POST-ing</h2>"));

                    } else {

                        sendResponse(buildHTMLBodyWithHomeButton(
                                "<h2>Method POST not allowed for " + uriParts[1] + "</h2>"));

                    }

                } else { // wrong HTTP method
                    // Send error 405 back ?
                    sendResponse(buildHTMLBodyWithHomeButton(
                            "<h2>Method " + method + " not allowed</h2>"));
                }


                if (uriParts[0].equals("sensors")) {

                    // Handle request and listen to event

                    SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


                } else if (uriParts[0].equals("actuators")) {

                    if( method.equals("POST") ) {

                        // TODO: read some values from the body and do something

                    } else { // wrong HTTP method
                        // Send error 405 back ?
                        sendResponse(buildHTMLBodyWithHomeButton(
                                "<h2>Method " + method + " not allowed for actuators</h2>"));
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private String buildHTMLBodyWithHomeButton(String body){
            return buildHTMLBody( "<a href=\"/\">&larr; Home</a><br>" + body);
        }

        private String buildHTMLBody(String body){
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<title>VS Pascalo</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    body +
                    "</body>\n" +
                    "</html>";
        }

        private void sendResponse(String html_doc) {

            int bodyLength = 9001;

            try {
                // the get bytes is important because UTF-8 characters can be longer than one byte each
                bodyLength = html_doc.getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                // da passiert eh nöd
            }

            String header = new StringBuilder(256).append("HTTP/1.1 ").append(" 200 OK\r\n")
                    .append("Server: ").append("Dini Mueter").append("\r\n")
                    .append("Content-Length: ").append(bodyLength).append("\r\n")
                    .append("Cache-Control: no-cache\r\n")
                    .append("Content-Type: text/html; charset=utf-8\r\n")
                    .append("Connection: close\r\n\r\n").toString();


            try {
                // outputStream can be used to send a response back to the client
                OutputStream outputStream = mSocket.getOutputStream();


                // printing html document
                PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                printwriter.print(header);
                printwriter.print(html_doc);
                printwriter.flush();


                // Properly close the socket to release resources, this will automatically close
                // the input and output streams as well
                mSocket.close();

                Log.d(TAG, "sending response: \n" + html_doc);

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
