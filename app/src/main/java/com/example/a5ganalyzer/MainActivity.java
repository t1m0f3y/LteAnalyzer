package com.example.a5ganalyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;


/*
    TODO:
        1. Вывести на экране телефона информацию по всем полям (Cell ID, RSRP, RSRQ, TA и т.д.).
        2. Сохранить данные на на телефоне (формат на свое усмотрение).
        3. Создать окно с выводом графиков по значению RSRP, RSRQ.
        4. Отправить данные на сервер.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_READ_PERMISSION = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION = 200;
    private String TAG = "CellInfo";
    private PhoneStateListener MyListener;
    private LocationListener locationListener;
    TelephonyManager tm;
    LocationManager locationManager;

    Location globalLocation;

    TextView textView;

    String url = "http://159.65.87.37/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text1);

        MyListener=new PhoneStateListener(){
            @Override
            //TODO: create ASK\Accept permissions
            // Without permission it is not working
            public void onSignalStrengthsChanged(SignalStrength Strength){
                if(ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                if(cellInfoList != null) {
                    for (CellInfo cellInfo : cellInfoList) {
                        if (cellInfo instanceof CellInfoLte) {
                            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                            int rsrp = 0;
                            int rsrq = 0;
                            double lat = 0;
                            double lon = 0;
                            if (globalLocation != null) {
                                lat = globalLocation.getLatitude();
                                lon = globalLocation.getLongitude();
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                rsrp = cellInfoLte.getCellSignalStrength().getRsrp();
                                rsrq = cellInfoLte.getCellSignalStrength().getRsrq();
                            }
                            Log.d("RSRP", Integer.toString(rsrp));
                            Log.d("RSRQ", Integer.toString(rsrq));
                            Date date = java.util.Calendar.getInstance().getTime();
                            askPermissionAndWriteFile("data", date.toString() + " "
                                    + Integer.toString(rsrp) + " " + Integer.toString(rsrq) + " "
                                    + Double.toString(lat) + " "
                                    + Double.toString(lon) + "\n");

                            textView.setText(cellInfoLte.toString()+"\n"
                                    + Double.toString(lat) + " "
                                    + Double.toString(lon) + "\n");

                            try {
                                URLConnection connection = new URL(url).openConnection();
                                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                connection.setConnectTimeout(10000);
                                connection.setDoInput(true);
                                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                                String reqBody = Double.toString(lat) + " " + Double.toString(lon);
                                writer.write(reqBody);
                                writer.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                globalLocation = loc;
                Log.d("location","location changed");
            }
        };

        // This one is working with permissions
        Log.d(TAG, "Hello");
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, tm.getNetworkOperatorName().toString());
        tm.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Log.d(TAG,"listening");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 1, locationListener);

    }

    private void writeToFile(String fileName,String content) {
        //File path = getApplicationContext().getFilesDir();
        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File path = getAppExternalFilesDir();
        try {
            //FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            FileWriter fr = new FileWriter(new File(path, fileName), true);
            fr.write(content);
            fr.close();
            Log.d("FILE", "wrote to file:" + path + '/' + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void askPermissionAndWriteFile(String fileName,String content) {
        boolean canWrite = this.askPermission(REQUEST_ID_WRITE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(!canWrite)  {
            Toast.makeText(getApplicationContext(),
                    "You do not allow this app to write files.", Toast.LENGTH_LONG).show();
            return;
        }
        //
        this.writeToFile(fileName, content);
    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private boolean askPermission(int requestId, String permissionName) {


        Log.i("log", "Ask for Permission: " + permissionName);
        Log.i("log", "Build.VERSION.SDK_INT: " + android.os.Build.VERSION.SDK_INT);

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);

            Log.i("log", "permission: " + permission);
            Log.i("log", "PackageManager.PERMISSION_GRANTED: " + PackageManager.PERMISSION_GRANTED);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{permissionName},
                        requestId
                );
                return false;
            }
        }
        return true;
    }

    // As soon as the user decides, allows or doesn't allow.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_ID_READ_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //readFile();
                    }
                }
                case REQUEST_ID_WRITE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //writeToFile();
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Permission Cancelled!", Toast.LENGTH_SHORT).show();
        }
    }

    // IMPORTANT!!
    public File getAppExternalFilesDir()  {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            // /storage/emulated/0/Android/data/org.o7planning.externalstoragedemo/files
            return this.getExternalFilesDir(null);
        } else {
            // @Deprecated in API 29.
            // /storage/emulated/0
            return Environment.getExternalStorageDirectory();
        }
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