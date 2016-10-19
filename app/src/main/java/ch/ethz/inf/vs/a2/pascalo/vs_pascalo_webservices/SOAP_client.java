package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.sensor.XmlSensor;

public class SOAP_client extends AppCompatActivity implements SensorListener,  View.OnClickListener {

    private XmlSensor mXmlSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_client);

        mXmlSensor = new XmlSensor();
        findViewById(R.id.soap_client_manual_button).setOnClickListener(this);
    }

    @Override
    public void onReceiveSensorValue(double value) {

    }

    @Override
    public void onReceiveMessage(String message) {

    }

    @Override
    public void onClick(View v) {
        mXmlSensor.getTemperature();
    }
}
