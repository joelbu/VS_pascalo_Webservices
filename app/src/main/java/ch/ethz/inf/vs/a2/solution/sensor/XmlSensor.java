package ch.ethz.inf.vs.a2.solution.sensor;

import java.net.HttpURLConnection;
import java.net.URL;


import ch.ethz.inf.vs.a2.http.RemoteServerConfiguration;
import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

/**
 * Created by Joel on 17.10.2016.
 */

public class XmlSensor extends AbstractSensor{

    private final String mPath = "/SunSPOTWebServices/SunSPOTWebservice?wsdl";

    @Override
    public String executeRequest() throws Exception {
        URL url = new URL("http", RemoteServerConfiguration.HOST, RemoteServerConfiguration.SOAP_PORT, mPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept", "text/xml");
        return null;
    }

    @Override
    public double parseResponse(String response) {

        return 0;
    }
}
