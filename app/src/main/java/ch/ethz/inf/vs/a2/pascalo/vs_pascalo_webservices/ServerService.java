package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Joel on 18.10.2016.
 */

public class ServerService extends Service {
    private final String TAG = "ServerService";
    private Webserver mWebserver;
    private int mPort = 8088;


    // We do not need this, but per the documentation we need to return null
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This sets the isInterrupted flag in the Webserver and terminates interruptible methods
        mWebserver.interrupt();
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
        Socket mSocket;

        public RequestHandler(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            Log.d(RHTAG, "There was a request that reached the request handler from address: " + mSocket.getInetAddress());

            try {
                // Reading the request? Nah, I don't care.
                OutputStream outputStream = mSocket.getOutputStream();

                // printing a constant in text/plain mode should be fine, right?
                PrintWriter printwriter = new PrintWriter(outputStream);
                printwriter.print("66.66");
                printwriter.flush();
                //printwriter.close();






                // Properly close the socket to release resources
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
