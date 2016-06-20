package com.example.lukaz.secondcv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FirstActivity extends AppCompatActivity {

    private static final String TAG = "Zacetna stran";
    int lock; // the lock for the proper button text (Start, Pause, Continue)
    String btnTxt = "";
    SharedPreferences mPrefs;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lock = getLock();
        if (lock==0) {
            btnTxt = "Start";
        }
        else if (lock ==1) {
            btnTxt = "Continue";
        }
        else { //lock == 2
            btnTxt = "Pause";
            delay();
        }

        //Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        mPrefs = getPreferences(MODE_PRIVATE);

        setContentView(R.layout.activity_first);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button b2 = (Button) findViewById(R.id.button2); //button Stop
        b2.setEnabled(false);

        b2.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        lock = 0;
                        saveLock();
                        Intent toResults = new Intent(FirstActivity.this, ResultsActivity.class);
                        FirstActivity.this.startActivity(toResults);
                        FirstActivity.this.finish();
                    }
                }
        );

        final Button b = (Button) findViewById(R.id.button1); //button Start (Pause, Continue)
        b.setText(btnTxt);
        b.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (lock == 0) {
                            btnTxt = "Pause";
                            b.setText(btnTxt);
                            lock = 2;
                            saveDate(); // save in txt file the time when the measurements started
                            saveLock(); // save the lock value for the proper button text
                            delay(); // delay 50s for the next reading
                        }
                        else if (lock == 1) {
                            btnTxt = "Pause";
                            b.setText(btnTxt);
                            lock = 2;
                            saveLock();
                            delay();
                        }
                        else { //lock == 2
                            btnTxt = "Continue";
                            b.setText(btnTxt);
                            b2.setEnabled(true);
                            lock = 1;
                            saveLock();
                            delayRemove(); //remove the delay of 50s
                        }
                    }
                }
        );
    }

    public void onPause() {
        super.onPause();
        //Log.d(TAG, "called onPause");
    }

    public void onStop() {
        super.onStop();
        //Log.d(TAG, "called onStop");
    }

    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "called onDestroy");
    }

    //delay 50s to the next reading
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

    //remove the delay of 50s
    public void delayRemove() {
        handler.removeCallbacksAndMessages(null);
    }

    // save the lock value for the proper button text (Start, Pause, Continue)
    public void saveLock() {
        SharedPreferences sharedPref = getSharedPreferences("buttonTxt", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("txt", lock);
        editor.apply();
    }

    // get the lock value for the proper button text (Start, Pause, Continue)
    public int getLock() {
        try {
        SharedPreferences sharedPref = getSharedPreferences("buttonTxt", Context.MODE_PRIVATE);
        int text = sharedPref.getInt("txt", 0);
        return  text;
        } catch (Exception e) {
              Log.d(TAG, "dispalyData: " + e.toString());
            return 0;
        }
    }

    public void saveDate() {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date());
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(openFileOutput("dateFile.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(timeStamp);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }
}

