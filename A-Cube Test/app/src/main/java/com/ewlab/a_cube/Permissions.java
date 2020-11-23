package com.ewlab.a_cube;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class Permissions extends AppCompatActivity {
    private static final String TAG = "Permissions";


    private final int PERMISSION_MULTIPLE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //verifica dei permessi
        int a = 0;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            Log.d("PERMESSI", "controllo versione");


            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                a = 1;
                Log.d("PERMESSI", "scrittura lettura dati");

            }

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                a = 2;
                Log.d("PERMESSI", "microfono");

            }

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                a = 3;
                Log.d("PERMESSI", "1 e 2");

            }
        }


        switch(a) {
            case 1:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_MULTIPLE);
            case 2:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSION_MULTIPLE);
            case 3:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        PERMISSION_MULTIPLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("PERMESSI", "controller permessi");
        switch(requestCode){
            case PERMISSION_MULTIPLE:
                if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)){
                    Log.d("PERMESSI", "Permits Granted");
                    Intent intent = new Intent(getApplicationContext(), RecorderService.class);
                    intent.putExtra("Permits","Granted");
                    startService(intent);
                    finish();


                }else{
                    Log.d("PERMESSI", "Permits Denied");
                    Toast.makeText(this,"Permits Denied", Toast.LENGTH_LONG);
                    Intent intent = new Intent(getApplicationContext(), RecorderService.class);
                    intent.putExtra("Permits","Denied");
                    startService(intent);
                    finish();
                }
        }
    }
}

