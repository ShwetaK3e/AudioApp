package com.shwetak3e.audioapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT=3;
    boolean splashTime=false;
    private Dialog myDialog=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    splashTime=true;
                    checkPermissionsAndProceed();
                }
            }, SPLASH_SCREEN_TIMEOUT * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(splashTime){
            checkPermissionsAndProceed();
        }
    }

    private void checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.M && !permissionsGranted()) {
            showMarshmallowPermissionDialogue();
        }
        else {
            proceed();
        }
    }

    private boolean permissionsGranted(){
        boolean recordAudio=getPermissionStatus(this, Manifest.permission.RECORD_AUDIO);
        boolean storage=getPermissionStatus(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (recordAudio && storage);
    }

    private boolean getPermissionStatus(Activity activity, String androidPermissionName) {
        if (ContextCompat.checkSelfPermission(activity, androidPermissionName) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionName)) {
                return false;
            }
            return false;
        }
        return true;
    }


    public void showMarshmallowPermissionDialogue() {

        myDialog = new Dialog(this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.custom_alert_dialog);
        myDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView alert_head = (TextView) myDialog
                .findViewById(R.id.alert_head);
        TextView alert_text = (TextView) myDialog
                .findViewById(R.id.alert_text);
        alert_head.setText("READ CONTACTS");
        alert_text.setText("NEED SOME PERMISSIONS TO PROCEED");
        Button yes = (Button) myDialog.findViewById(R.id.alert_yes);
        Button no = (Button) myDialog.findViewById(R.id.alert_no);
        yes.setText("OK");

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                givePermissionforMashmallow(SplashActivity.this);

            }
        });
        no.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                Toast.makeText(SplashActivity.this, "Cannot show Contacts.Permission Not Granted", Toast.LENGTH_LONG).show();
                checkPermissionsAndProceed();
            }
        });
        myDialog.show();
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(false);
    }


    public static void givePermissionforMashmallow(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }


    public void proceed(){
        Intent intent=new Intent(this,VokesListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
