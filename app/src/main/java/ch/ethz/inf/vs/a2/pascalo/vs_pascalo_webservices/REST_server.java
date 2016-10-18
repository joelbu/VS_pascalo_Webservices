package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Enumeration;

import ch.ethz.inf.vs.a2.sensor.SensorListener;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class REST_server extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "RESTServer";
    private NetworkInterface mWlanInterface;
    private TextView mTextViewServiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);

        mTextViewServiceStatus = (TextView) findViewById(R.id.rest_server_status_text);
        findViewById(R.id.rest_server_start).setOnClickListener(this);
        findViewById(R.id.rest_server_stop).setOnClickListener(this);

        Enumeration<NetworkInterface> interfaces;

        try {
            interfaces = getNetworkInterfaces();
            NetworkInterface current;
            while (interfaces.hasMoreElements()) {
                current = interfaces.nextElement();
                String interfaceDisplayName = current.getDisplayName();
                Log.d(TAG, interfaceDisplayName);
                if (interfaceDisplayName.contains("wlan0")) {
                    mWlanInterface = current;
                    Log.d(TAG, "Found wlan0!");
                }
            }




        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rest_server_start:

                break;
            case R.id.rest_server_stop:

                break;
        }
    }
}
