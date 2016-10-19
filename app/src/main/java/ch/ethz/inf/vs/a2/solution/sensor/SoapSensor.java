package ch.ethz.inf.vs.a2.solution.sensor;

import android.util.Log;

import ch.ethz.inf.vs.a2.http.RemoteServerConfiguration;
import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Joel on 17.10.2016.
 */

public class SoapSensor extends AbstractSensor {
    private final String TAG = "SoapSensor";

    private final String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private final String METHOD_NAME = "getSpot";
    private final String SOAP_ACTION = "http://webservices.vslecture.vs.inf.ethz.ch/getSpot";
    private final String PATH = "/SunSPOTWebServices/SunSPOTWebservice";


    SoapSerializationEnvelope test;
    SoapObject mSoapObject;

    @Override
    public String executeRequest() throws Exception {
        String response = "";
        String temperatureString = "-1000.0";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        request.addProperty("id", "Spot3");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.setOutputSoapObject(request);

        HttpTransportSE transport = new HttpTransportSE("http://" + RemoteServerConfiguration.HOST + ":" + String.valueOf(RemoteServerConfiguration.SOAP_PORT) + PATH);

        try {

            transport.call(SOAP_ACTION, envelope);
            SoapObject soap =  (SoapObject) envelope.bodyIn;
            temperatureString = ((SoapObject) soap.getProperty(0)).getProperty(5).toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return temperatureString;
    }

    @Override
    public double parseResponse(String response) {
        return Double.parseDouble(response);
    }
}
