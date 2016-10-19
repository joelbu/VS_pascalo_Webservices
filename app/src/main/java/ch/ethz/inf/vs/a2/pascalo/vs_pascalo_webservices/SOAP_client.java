package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.sensor.SoapSensor;
import ch.ethz.inf.vs.a2.solution.sensor.XmlSensor;

public class SOAP_client extends AppCompatActivity implements SensorListener,  View.OnClickListener {

    private XmlSensor mXmlSensor;
    private SoapSensor mSoapSensor;
    private TextView mResponseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_client);

        mResponseTextView = (TextView) findViewById(R.id.soap_client_temperature_display);
        findViewById(R.id.soap_client_manual_button).setOnClickListener(this);
        findViewById(R.id.soap_client_library_button).setOnClickListener(this);

        mXmlSensor = new XmlSensor();
        mXmlSensor.registerListener(this);

        mSoapSensor = new SoapSensor();
        mSoapSensor.registerListener(this);

    }

    @Override
    public void onReceiveSensorValue(double value) {
        mResponseTextView.setText(String.valueOf(value));
    }

    @Override
    public void onReceiveMessage(String message) {

    }

    @Override
    public void onClick(View v) {
        // Register the buttons to the sensors
        switch (v.getId()) {
            case R.id.soap_client_manual_button:
                mXmlSensor.getTemperature();
                break;
            case R.id.soap_client_library_button:
                mSoapSensor.getTemperature();
                break;
        }
    }
}
