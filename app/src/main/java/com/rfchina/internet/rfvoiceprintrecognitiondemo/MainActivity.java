package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.acousticWave.AcousticWaveActivity;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth.BluetoothClientActivity;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth.BluetoothWelcomeActivity;

public class MainActivity extends Activity {
    private TextView txtVoicePrintWelcome;
    private TextView txtAcousticWaveWelcome;
    private TextView txtBluetoothWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtVoicePrintWelcome = (TextView) findViewById(R.id.txtVoicePrintWelcome);
        txtAcousticWaveWelcome = (TextView) findViewById(R.id.txtAcousticWaveWelcome);
        txtBluetoothWelcome = (TextView) findViewById(R.id.txtBluetoothWelcome);
        txtVoicePrintWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VoiceprintWelcomeActivity.class));
            }
        });
        txtAcousticWaveWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AcousticWaveActivity.class));
            }
        });
        txtBluetoothWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BluetoothClientActivity.class));
            }
        });

        Log.d("ddd","onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ddd","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ddd","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ddd","onStop");
    }
}
