package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.sensor.RawHttpSensor;

public class REST_client extends AppCompatActivity implements SensorListener, View.OnClickListener  {

    private final String TAG = "RestClient";

    private RawHttpSensor mRawHttpSensor;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_client);

        // Create RawHttpSensor and make our activity a listener of it, so we can display the result
        mRawHttpSensor = new RawHttpSensor();
        mRawHttpSensor.registerListener(this);

        mTextView = (TextView) findViewById(R.id.rest_client_text_view);
        findViewById(R.id.rest_client_again_button).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        makeRawHttpSensorRequest();
    }

    private void makeRawHttpSensorRequest() {

        mRawHttpSensor.getTemperature();

        /* Shit all of this is already in the abstract class

        // Network activity needs to be done on another thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String response = "";
                try {
                    response = mRawHttpSensor.executeRequest();
                } catch (Exception e) {
                    Log.e(TAG, "Caught exception from mRawHttpSensor " + e.toString());
                }
                double temperature = mRawHttpSensor.parseResponse(response);
                return null;
            }
        }.execute();

        */
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
        switch (v.getId()) {
            case R.id.rest_client_again_button:
                makeRawHttpSensorRequest();
                break;
        }
    }
}
