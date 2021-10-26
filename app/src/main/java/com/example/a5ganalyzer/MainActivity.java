package com.example.a5ganalyzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "CellInfo";
    private PhoneStateListener MyListener;
    TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyListener=new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength Strength){
                if(ActivityCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                Log.d(TAG,String.valueOf(cellInfoList));
                for(CellInfo cellInfo:cellInfoList){
                    if(cellInfo instanceof CellInfoLte){
                        CellInfoLte cellInfoLte=(CellInfoLte)cellInfo;
                        Log.d(TAG,cellInfoLte.getCellSignalStrength().toString());
                    }
                }
            }
        };
        Log.d(TAG,"Hello");
        tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG,tm.getNetworkOperatorName().toString());
        tm.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Log.d(TAG,"listening");
    }

}