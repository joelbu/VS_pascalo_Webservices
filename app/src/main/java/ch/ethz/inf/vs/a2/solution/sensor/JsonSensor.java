package ch.ethz.inf.vs.a2.solution.sensor;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

/**
 * Created by Joel on 17.10.2016.
 */

public class JsonSensor extends AbstractSensor {
    private final String TAG = "JsonSensor";

    private final String mHost = "vslab.inf.ethz.ch";
    private final int mPort = 8081;
    private final String mPath = "/sunspots/Spot1/sensors/temperature/";

    @Override
    public String executeRequest() throws Exception {
        URL url = new URL("http", mHost, mPort, mPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Setting the request header and hoping the rest is okay by default
        connection.setRequestProperty("Accept", "application/json");

        // Get the stream from the connection
        InputStream inputStream = connection.getInputStream();

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
        JSONObject json;
        double value;
        try {
            json = new JSONObject(response);
            value = json.getDouble("value");
        } catch (Exception e) {
            e.printStackTrace();
            return -1000.0;
        }

        return value;
    }
}
