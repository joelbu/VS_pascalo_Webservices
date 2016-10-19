package ch.ethz.inf.vs.a2.solution.sensor;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;


import ch.ethz.inf.vs.a2.http.RemoteServerConfiguration;
import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

/**
 * Created by Joel on 17.10.2016.
 */

public class XmlSensor extends AbstractSensor{
    private final String TAG = "AbstractSensor";
    private final String mPath = "/SunSPOTWebServices/SunSPOTWebservice";

    @Override
    public String executeRequest() throws Exception {
        URL url = new URL("http", RemoteServerConfiguration.HOST, RemoteServerConfiguration.SOAP_PORT, mPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml");

        // Write the request into the socket thereby sending it to the server
        PrintWriter printwriter = new PrintWriter(connection.getOutputStream());
        printwriter.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <S:Header/>\n" +
                "    <S:Body>\n" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">\n" +
                "            <id>Spot3</id>\n" +
                "        </ns2:getSpot>\n" +
                "    </S:Body>\n" +
                "</S:Envelope>");
        printwriter.flush();

        // Read the response stream from the socket
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
        Log.d(TAG, response);
        double result = -1000.0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new StringReader(response ) );
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("temperature")) {
                    xpp.next();
                    result = Double.parseDouble(xpp.getText());
                }
                eventType = xpp.next();
            }

        }
        catch(Exception e) {
            // Do nothing
            Log.d(TAG, "Pull parser failed");
            e.printStackTrace();
        }
        return result;
    }
}
