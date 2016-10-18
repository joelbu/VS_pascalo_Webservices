package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Enumeration;

import ch.ethz.inf.vs.a2.sensor.SensorListener;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class REST_server extends AppCompatActivity {
    private static String TAG = "RESTServer";
    NetworkInterface mWlanInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);

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

}
