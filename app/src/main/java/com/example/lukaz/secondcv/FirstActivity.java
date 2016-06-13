package com.example.lukaz.secondcv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class FirstActivity extends AppCompatActivity {

    private static final String TAG = "OCVSample::Activity";
   //error
   boolean clicked;
    String btnTxt = "";
    SharedPreferences mPrefs;
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        clicked = displayData();
        if ( clicked == false) {
            btnTxt = "Start";
        }
        else {
            btnTxt = "Pause";
            delay();
        }
        Log.d(TAG, "1 called onCreate");
        super.onCreate(savedInstanceState);
        mPrefs = getPreferences(MODE_PRIVATE);

        //Log.d(TAG, "1 called onCreate3");
        setContentView(R.layout.activity_first);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Button b2 = (Button) findViewById(R.id.button2);
        b2.setEnabled(false);

        b2.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        //b2.setText("OK");
                        Intent toResults = new Intent(FirstActivity.this, ResultsActivity.class);
                        FirstActivity.this.startActivity(toResults);
                        FirstActivity.this.finish();
                    }
                }
        );

        final Button b = (Button) findViewById(R.id.button1);
        b.setText(btnTxt);
        b.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (clicked == false) {
                            btnTxt = "Pause";
                            b.setText(btnTxt);
                            clicked = true;
                            saveInfo();
                            delay();
                        } else {
                            btnTxt = "Start";
                            b.setText(btnTxt);
                            clicked = false;
                            b2.setEnabled(true);
                            saveInfo();
                            delay2();
                        }
                    }
                });

        //b.setText(btnTxt);
        /*
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final int status =(Integer) v.getTag();
                //if(status == 1) {
                //b.setText("Pause");
                   /* btnTxt = b.getText().toString();
                b.setText(mPrefs.getString("btnTxt", btnTxt));


                SharedPreferences.Editor ed = mPrefs.edit();
                ed.putString(btnTxt, btnTxt);
                ed.commit();*/
            /*    clicked = true;
                //v.setTag(0);
                Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                startActivity(intent);
                //pause
                //} //else {
                // b.setText("Start!!");
                //  v.setTag(1); //pause
                //}
            }
        });*/


    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "1 called onPause");
        //finish();
    }

    public void onStop() {
        super.onStop();
        //Log.d(TAG, "1 called onStop");

    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "1 called onDestroy");
    }

    public void pausePro(View view) {
        clicked = false;
    }

    public void delay() {
        final Runnable ri = new Runnable() {
            public void run() {
                Intent mInHome = new Intent(FirstActivity.this, MainActivity.class);
                FirstActivity.this.startActivity(mInHome);
                FirstActivity.this.finish();
            }
        };
        handler.postDelayed(ri, 10000);
    }

    public void delay2() {
        handler.removeCallbacksAndMessages(null);
    }

    public void saveInfo() {
        SharedPreferences sharedPref = getSharedPreferences("buttonTxt", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("txt", clicked);
        editor.apply();
    }

    public boolean displayData() {
        //try {
            SharedPreferences sharedPref = getSharedPreferences("buttonTxt", Context.MODE_PRIVATE);
            Boolean text = sharedPref.getBoolean("txt", false);
            return  text;
        //} catch (Exception e) {
        //    Log.d(TAG, "dispalyData: " + e.toString());
        //    return false;
        //}

    }
}

