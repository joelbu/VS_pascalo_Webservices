package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_rest_client).setOnClickListener(this);
        findViewById(R.id.button_soap_client).setOnClickListener(this);
        findViewById(R.id.button_rest_server).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_rest_client:
                startActivity(new Intent(getApplicationContext(), REST_client.class));
                break;
            case R.id.button_soap_client:
                startActivity(new Intent(getApplicationContext(), SOAP_client.class));
                break;
            case R.id.button_rest_server:
                startActivity(new Intent(getApplicationContext(), REST_server.class));
                break;
            default:
                Log.e(TAG, "onClick got called with an unexpected view.");
                finish();
                break;
        }

    }
}
