package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.solution.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.solution.sensor.TextSensor;

public class REST_client extends AppCompatActivity implements SensorListener, View.OnClickListener  {

    private final String TAG = "RestClient";

    private RawHttpSensor mRawHttpSensor;
    private TextSensor mTextSensor;
    private TextView mTextView;
    private JsonSensor mJsonSensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_client);

        // Create RawHttpSensor and make our activity a listener of it, so we can display the result
        mRawHttpSensor = new RawHttpSensor();
        mRawHttpSensor.registerListener(this);

        // Create TextSensor and make our activity a listener of it, so we can display the result
        mTextSensor = new TextSensor();
        mTextSensor.registerListener(this);

        // Create JsonSensor and make our activity a listener of it, so we can display the result
        mJsonSensor = new JsonSensor();
        mJsonSensor.registerListener(this);

        // Find the TextView for displaying results
        mTextView = (TextView) findViewById(R.id.rest_client_text_view);
        findViewById(R.id.rest_client_raw_button).setOnClickListener(this);
        findViewById(R.id.rest_client_text_request).setOnClickListener(this);
        findViewById(R.id.rest_client_json_button).setOnClickListener(this);
    }

    @Override
    public void onReceiveSensorValue(double value) {

        // We need to change UI text on the UI thread so whatever, this works I guess
        final double finalValue = value;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(getText(R.string.temperature_is) + " " + String.valueOf(finalValue) + getText(R.string.degree_symbol));
            }
        });

    }

    @Override
    public void onReceiveMessage(String message) {

    }

    @Override
    public void onClick(View v) {
        // Register the Again Button to the TextSensor
        switch (v.getId()) {
            case R.id.rest_client_raw_button:
                mRawHttpSensor.getTemperature();
                break;
            case R.id.rest_client_text_request:
                mTextSensor.getTemperature();
                break;
            case R.id.rest_client_json_button:
                mJsonSensor.getTemperature();
                break;
        }
    }
}
