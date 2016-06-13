package com.example.lukaz.secondcv;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Moments;
import android.view.SurfaceView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat mRgba;
    private Mat mHsv;
    private Mat mMask;
    private Mat mDilated;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat hierarchy;

    double[] res = {0,0,0,0,0,0};
    double[] rezultat= {0,0,0,0,0,0};

    double minX = 99999;
    double maxX = 0;
    double minY = 99999;
    double maxY = 0;
    int visina = 0;
    int najvisjaT = 0;
    int srednjaT = 0;
    int najnizjaT =0;
    double prvaTX = 0;
    double drugaTX = 0;
    double tretjaTX = 0;
    double prvaTY = 0;
    double drugaTY = 0;
    double tretjaTY =0;
    double distance1 = 9999;
    double distance2 = 9999;
    double distance3 = 9999;

    double spodnjiKot = 0;
    double zgornjiKot = 0;

    int counter = 0;
    double kot1Skupaj = 0;
    double  kot2Skupaj = 0;
    
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 80);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener( MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "2 called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent mInHome = new Intent(MainActivity.this, FirstActivity.class);
                MainActivity.this.startActivity(mInHome);
                MainActivity.this.finish();
            }
        }, 5000);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "2 called onPause");
        kot1Skupaj = zgornjiKot/counter;
        kot2Skupaj = spodnjiKot/counter;
        Log.d(TAG, "kot1Skupaj (0.173648) = " + kot1Skupaj);
        Log.d(TAG, "kot2Skupaj (0.5) = " + kot2Skupaj);
        int opozorilo = 0;
        //Preglje tudi če je NaN, čeprav dela tudi tako.
        // send the tone to the "alarm" stream (classic beeps go there) with 80% volume
        if ((Math.abs(kot1Skupaj) > 0.173648) || (Math.abs(kot2Skupaj) > 0.5)) {
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 is duration in ms
            opozorilo = 1;
        }

        writeToFile(opozorilo + ", " + Math.toDegrees(Math.acos(kot1Skupaj)) + ", " + Math.toDegrees(Math.acos(kot2Skupaj)));
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "2 called onResume");
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
         //       mLoaderCallback);

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "2 called onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mHsv = new Mat();
        mMask = new Mat();
        hierarchy = new Mat();
        mDilated = new Mat();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.flip(mRgba, mRgba, 0);
        //Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.cvtColor(mRgba, mHsv, Imgproc.COLOR_RGB2HSV);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Scalar lower = new Scalar(60, 0, 0);
        Scalar upper = new Scalar(150, 255, 255);
        Core.inRange(mHsv, lower, upper, mMask);;
        //Core.bitwise_or(mMask1, mMask2, mMask1);
        Imgproc.dilate(mMask, mDilated, new Mat());
        //Core.flip(mDilated, mDilated, 1);

        Imgproc.findContours(mDilated, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        /* // APPROXPOLYDP
        for(int i=0;i<contours.size();i++){
            contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 5, true);
            //double iu = Imgproc.arcLength(mMOP2f2, true);
            mMOP2f2.convertTo(contours.get(i), CvType.CV_32S);
        }*/

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

            /* //ZA PROVAT
            double maxArea = 0;
            if (Imgproc.contourArea(contours.get(contourIdx)) > maxArea) {
                maxArea = Imgproc.contourArea(contours.get(contourIdx));
            }
            contours.get(contourIdx).size()
            */
            
            if (Imgproc.contourArea(contours.get(contourIdx)) > 4000)  // Minimum size allowed for consideration
            {
                // CENTROID CONTURE
                Moments moments = Imgproc.moments(contours.get(contourIdx));
                Point centroid = new Point();
                centroid.x = moments.get_m10() / moments.get_m00();
                centroid.y = moments.get_m01() / moments.get_m00();

                final Point[] contArray = contours.get(contourIdx).toArray();

                //Thread t1 = new Thread(new Runnable() {
                 //   public void run() {
                        // code goes here.
                        try {
                            res = Elaborate(contArray, centroid);
                            double prvaTockaX = res[0];
                            double prvaTockaY = res[1];
                            double drugaTockaX = res[2];
                            double drugaTockaY = res[3];
                            double tretjaTockaX = res[4];
                            double tretjaTockaY = res[5];

                            spodnjiKot = spodnjiKot + Angle(new Point(tretjaTockaY, (tretjaTockaX - 200)), new Point(drugaTockaY, drugaTockaX), new Point(tretjaTockaY, tretjaTockaX));
                            zgornjiKot = zgornjiKot + Angle(new Point(drugaTockaY, drugaTockaX-200), new Point(prvaTockaY, prvaTockaX), new Point(drugaTockaY, drugaTockaX));
                            counter++;

                            // send the tone to the "alarm" stream (classic beeps go there) with 80% volume
                            /*if ((Math.abs(spodnjiKot) > 0.173648) || (Math.abs(zgornjiKot) > 0.5)) {
                                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 is duration in ms
                            }*/
                            /*
                            Imgproc.drawMarker(mRgba, new Point(prvaTockaY, prvaTockaX), new Scalar(0, 255, 0));
                            Imgproc.drawMarker(mRgba, new Point(drugaTockaY, drugaTockaX), new Scalar(0, 255, 0));
                            Imgproc.drawMarker(mRgba, new Point(tretjaTockaY, tretjaTockaX), new Scalar(0, 255, 0));

                            Imgproc.drawMarker(mRgba, centroid, new Scalar(0, 0, 255)); //Centroid
                            */
                            Imgproc.line(mRgba, new Point(prvaTockaY, prvaTockaX), new Point(drugaTockaY, drugaTockaX), new Scalar(0, 255, 0), 4);
                            Imgproc.line(mRgba, new Point(drugaTockaY, drugaTockaX), new Point(tretjaTockaY, tretjaTockaX), new Scalar(0, 255, 0), 4);
                            Imgproc.drawContours(mRgba, contours, contourIdx, new Scalar(255, 0, 0), 2);
                            
                        } catch (Exception e) {
                            Log.d("logic", "1: " + e.toString());
                        }
                //    }});
               // t1.start();
            }

            prvaTX = 0;
            drugaTX = 0;
            tretjaTX = 0;
            prvaTY = 0;
            drugaTY = 0;
            tretjaTY =0;
            minX = 0;
            maxX = 0;
            minY = 99999;
            maxY = 0;
            visina = 0;
            najvisjaT = 0;
            srednjaT = 0;
            najnizjaT = 0;
            distance1 = 9999;
            distance2 = 9999;
            distance3 = 9999;
            /*
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        return mRgba;
    }

    public double[]  Elaborate (final Point[] contArray, final Point center) {
        //Thread t1 = new Thread(new Runnable() {
            //public void run() {
                try {
                    // Pozor, x in y kordinate so zamenjane!
                    int arrLength = contArray.length;
                    for (int i = 0; i < arrLength; i++) {

                        if (contArray[i].x < minY) { // najnizja y tocka contoura
                            minY = contArray[i].x; //najnizji y
                            minX = contArray[i].y; //najnizji x
                        }

                        if (contArray[i].x > maxY) { // najvisja y tocka contoura
                            maxY = contArray[i].x; //najvisji y
                            maxX = contArray[i].y; //najvisji x
                        }
                    }
                    
                    visina = (int) (maxY - minY);
                    najvisjaT = (int)(maxY - (visina/10));
                    srednjaT = (int)( maxY - (visina/3));
                    najnizjaT = (int)(maxY - (5*visina/6));

                    distance1 = 9999;
                    distance2 = 9999;
                    distance3 = 9999;

                    for (int i = 0; i < arrLength; i++) {
                        if (contArray[i].y > (center.y)) {

                            if (Math.abs(contArray[i].x - najvisjaT) < distance1) {
                                distance1 = (Math.abs(contArray[i].x - najvisjaT));
                                prvaTX = contArray[i].y;
                                prvaTY = contArray[i].x;
                            }

                            if (Math.abs(contArray[i].x - srednjaT) < distance2) {
                                distance2 = (Math.abs(contArray[i].x - srednjaT));
                                drugaTX = contArray[i].y;
                                drugaTY = contArray[i].x;
                            }

                            if (Math.abs(contArray[i].x - najnizjaT) < distance3) {
                                distance3 = (Math.abs(contArray[i].x - najnizjaT));
                                tretjaTX = contArray[i].y;
                                tretjaTY = contArray[i].x;
                            }
                        }
                    }
                    rezultat = new double[]{prvaTX, prvaTY, drugaTX, drugaTY, tretjaTX, tretjaTY};
                } 
                catch (Exception e) {
                    Log.d("logic", "2: " + e.toString());
                }
            //}
        //});
        //t1.start();
        return rezultat;
    }
    
    // finds a cosine of angle between vectors
    // from pt0->pt1 and from pt0->pt2
    public static double Angle( Point pt1, Point pt2, Point pt0 )
    {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }
    /*
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }*/

    private void writeToFile(String data) {
        try {
            File folder = Environment.getExternalStorageDirectory();
            File dir = new File (folder.getAbsolutePath() + "/PostureCorrectoresults/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //File sdCard = Environment.getExternalStorageDirectory();
            //File directory = new File (sdCard.getAbsolutePath() + "/MyFiles");
            //directory.mkdirs();

            //Generira datum
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String timeStamp2 = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            //V datoteko potatki.txt pisemo izmerjene podatke.
            File outFile = new File(Environment.getExternalStorageDirectory() +"/PostureCorrectoresults/" ,timeStamp2+".txt");
            FileWriter fw = new FileWriter(outFile, true);
            fw.append(timeStamp+" ");
            fw.append(data);
            fw.append("\n");
            fw.close();

            //ZA REKORD
            //V datoteko FILENAME pišemo zadnji podatek, ki bo prekril prejsnjega. To rabi samo za prikaz v aplikaciji.
            /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(timeStamp+" ");
            outputStreamWriter.write(data);
            outputStreamWriter.write("\n");
            outputStreamWriter.close();*/

        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }
    //ZA REKORD
    /*
    private String readFromFile() {

        String ret = "";
        try {
            InputStream inputStream = openFileInput(FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG1, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG1, "Can not read file: " + e.toString());
        }

        return ret;
    }*/
}
