package ch.ethz.inf.vs.a2.solution.sensor;

import android.text.Html;
import android.util.Log;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import ch.ethz.inf.vs.a2.solution.http.HttpRawRequestImpl;

/**
 * Created by Joel on 17.10.2016.
 */

public class RawHttpSensor extends AbstractSensor {
    private final String TAG = "RawHttpSensor";

    private final int mPort = 8081;
    private final String mHost = "vslab.inf.ethz.ch";
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

        // Read the response from the socket
        char[] response = new char[2048];
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        inputStreamReader.read(response, 0, 2048);

        // Make a String from the character array and return that
        return String.valueOf(response);
    }

    @Override
    public double parseResponse(String response) {
        Log.d(TAG, "Arrived in parseResponse");
        // Since the unit test ensures we set the Accept header to text/html we are going to have
        // to find the value from within the HTML here, which is frankly ridiculous

        // getterValue is the HTML class attribute of the span containing the value, it is unique
        // within the page, so it should be safe to search for this
        int index = response.indexOf("getterValue");
        String valueText = response.substring(index+13, index+18);

        // check if we were unfortunate and only got one decimal after the period, throw out '<'
        if (-1 != valueText.indexOf('<')) {
            valueText = valueText.substring(0, 3);
        }

        double temperature = Double.valueOf(valueText).doubleValue();

        // This is confusing to me, the assignment tells us to register our activity as a listener,
        // so I publish the value here, right before returning it and do not use the return value
        // in the activity but instead the value we get in the listener
        return temperature;
    }
}
