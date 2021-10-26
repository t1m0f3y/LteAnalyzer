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


/*
    TODO:
        1. Вывести на экране телефона информацию по всем полям (Cell ID, RSRP, RSRQ, TA и т.д.).
        2. Сохранить данные на на телефоне (формат на свое усмотрение).
        3. Создать окно с выводом графиков по значению RSRP, RSRQ.
        4. Отправить данные на сервер.
 */
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
            //TODO: create ASK\Accept permissions
            // Without permission it is not working
            public void onSignalStrengthsChanged(SignalStrength Strength){
                if(ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                for(CellInfo cellInfo:cellInfoList){
                    if(cellInfo instanceof CellInfoLte){
                        CellInfoLte cellInfoLte=(CellInfoLte)cellInfo;
                        Log.d(TAG,cellInfoLte.getCellSignalStrength().toString());
                    }
                }
            }
        };
        // This one is working with permissions
        Log.d(TAG, "Hello");
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, tm.getNetworkOperatorName().toString());
        tm.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Log.d(TAG,"listening");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "On Start");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "On Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "On Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "On Stop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(TAG, "On Restart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "On Destroy");
    }

}