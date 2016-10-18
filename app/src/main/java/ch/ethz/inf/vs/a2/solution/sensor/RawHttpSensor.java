package ch.ethz.inf.vs.a2.solution.sensor;

import android.util.Log;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import ch.ethz.inf.vs.a2.solution.http.HttpRawRequestImpl;


public class RawHttpSensor extends AbstractSensor {
    private final String TAG = "RawHttpSensor";

    private final String mHost = "vslab.inf.ethz.ch";
    private final int mPort = 8081;
    private final String mPath = "/sunspots/Spot1/sensors/temperature/";

    @Override
    public String executeRequest() throws Exception {
        Log.d(TAG, "Arrived in executeRequest");

        // Bind to a port etc, pretty automatic, it even does DNS-lookup and everything for us
        Socket socket = new Socket(mHost, mPort);

        // Build our request with the other class we implemented previously
        HttpRawRequestImpl requestMaker = new HttpRawRequestImpl();
        String request = requestMaker.generateRequest(mHost, mPort, mPath);

        // Write the request into the socket thereby sending it to the server
        PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
        printwriter.print(request);
        printwriter.flush();

        // Read the response stream from the socket
        InputStream inputStream = socket.getInputStream();

        // This is the most efficient way to get a String from the stream apparently
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    @Override
    public double parseResponse(String response) {
        Log.d(TAG, "Arrived in parseResponse");
        // Since the unit test ensures we set the Accept header to text/html we are going to have
        // to find the value from within the HTML here, which is a bit roundabout

        // getterValue is the HTML class attribute of the span containing the value, it is unique
        // within the page, so it should be safe to search for this
        int index = response.indexOf("getterValue");
        String valueText = response.substring(index+13, index+18);

        // check if we were unfortunate and only got one decimal after the period, throw out '<'
        if (-1 != valueText.indexOf('<')) {
            valueText = valueText.substring(0, 3);
        }

        return Double.valueOf(valueText).doubleValue();
    }
}
