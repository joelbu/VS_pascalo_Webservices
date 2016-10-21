package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import ch.ethz.inf.vs.a2.sensor.SensorListener;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class REST_server extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "RESTServer";
    private NetworkInterface mWlanInterface;
    private InetAddress mInetAddress;
    private TextView mTextViewServiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);

        mTextViewServiceStatus = (TextView) findViewById(R.id.rest_server_status_text);
        findViewById(R.id.rest_server_start).setOnClickListener(this);
        findViewById(R.id.rest_server_stop).setOnClickListener(this);

        try {
            Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
            NetworkInterface currentInterface;
            while (interfaces.hasMoreElements()) {

                currentInterface = interfaces.nextElement();
                String interfaceDisplayName = currentInterface.getDisplayName();
                Log.d(TAG, interfaceDisplayName);

                if (interfaceDisplayName.contains("wlan0")) {
                    mWlanInterface = currentInterface;
                    Log.d(TAG, "Found wlan0!");
                    Enumeration<InetAddress> addresses = mWlanInterface.getInetAddresses();
                    InetAddress currentAddress = InetAddress.getLoopbackAddress();

                    while (addresses.hasMoreElements()) {
                        currentAddress = addresses.nextElement();
                        Log.d(TAG, currentAddress.toString());

                        // Hacky way to maybe select the IPv4 address?
                        if (currentAddress.getAddress().length == 4) {
                            mInetAddress = currentAddress;
                        }
                    }

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
                Log.d(TAG, "Click server start registered");
                Intent intent = new Intent(this, ServerService.class);

                // mInetAddress is null if, for example, the device has no internet connection
                if (mInetAddress != null){
                    intent.putExtra("IPAddress", mInetAddress);
                    startService(intent);
                    TextView infoDisplay = (TextView) findViewById(R.id.rest_server_status_text);
                    infoDisplay.setText(mInetAddress.toString().substring(2) + ":8088");
                }

                else {
                    Log.d(TAG, "No IP address found, server could not be started.");
                }
                break;
            case R.id.rest_server_stop:
                stopService(new Intent(this, ServerService.class));
                break;
        }
    }
}
