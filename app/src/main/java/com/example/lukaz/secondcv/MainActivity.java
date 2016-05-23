package com.example.lukaz.secondcv;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.view.View.OnTouchListener;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
/*
//public class MainActivity extends AppCompatActivity {
/*
    @Override
  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}*/

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat mRgba;
    private Mat mGray;
    private Mat mHsv;
    private Mat mMask;
    private Mat mDilated;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat hierarchy;
    private Mat mNew;
    private MatOfPoint2f mMOP2f1;
    private MatOfPoint2f mMOP2f2;

    int z = 0;

    double prvi = 0;
    double drugi = 0;
    double tretji = 0;
    double cetrti = 0;
    double[] res = {0,0,0,0};
    double[] rezultat= {0,0,0,0};

    double minY = 99999;
    double maxY = 0;
    double miY = 99999;
    double maY = 0;
    int visina = 0;
    int najvisjaT = 0;
    int srednjaT = 0;
    int najnizjaT =0;
    double prviX = 0;
    double drugiX = 0;
    double tretjiX =0;
    double distance = 9999;
    double distance2 = 9999;
    double distance3 = 9999;
    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);

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
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mHsv = new Mat();
        mMask = new Mat();
        hierarchy = new Mat();
        mDilated = new Mat();
        mNew = new Mat();
        //p = new Point(300, 300);

        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.flip(mRgba, mRgba, 0);
        //Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.cvtColor(mRgba, mHsv, Imgproc.COLOR_RGB2HSV);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<MatOfPoint2f> contours2 = new ArrayList<MatOfPoint2f>();

        Scalar lower = new Scalar(75, 32, 32);
        Scalar upper = new Scalar(125, 255, 255);
        Core.inRange(mHsv, lower, upper, mMask);
        Imgproc.dilate(mMask, mDilated, new Mat());
        MatOfPoint2f pt2f = new MatOfPoint2f();
        //Core.flip(mDilated, mDilated, 1);

        //contours.convertTo(pt2f, CvType.CV_32FC2);
        Imgproc.findContours(mDilated, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
/*
        for(int i=0;i<contours.size();i++){
            contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 5, true);
            //double iu = Imgproc.arcLength(mMOP2f2, true);
            mMOP2f2.convertTo(contours.get(i), CvType.CV_32S);
        }*/

        /*
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
        {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
                Imgproc.drawContours(mRgba, contours, idx, new Scalar(250, 0, 0),2);
            }
        }*/
        //contours.get(0);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            /*
                double maxArea = 0;
            if (Imgproc.contourArea(contours.get(contourIdx)) > maxArea) {
                maxArea = Imgproc.contourArea(contours.get(contourIdx));
            }
            contours.get(contourIdx).size()
            */
            if (Imgproc.contourArea(contours.get(contourIdx)) > 4000)  // Minimum size allowed for consideration
            //if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
            {
                Moments moments = Imgproc.moments(contours.get(contourIdx));
                Point centroid = new Point();
                centroid.x = moments.get_m10() / moments.get_m00();
                centroid.y = moments.get_m01() / moments.get_m00();

                final Point[] oo = contours.get(contourIdx).toArray();


                //String z = contours.get(contourIdx).size().toString();
                //MatOfPoint myIntArray = contours.get(contourIdx);
                //int r = myIntArray.length;

                //ArrayList<MatOfPoint> drugi = new ArrayList<MatOfPoint>();

                //Thread t1 = new Thread(new Runnable() {
                 //   public void run() {
                        // code goes here.
                        try {
                            //if (oo.length >= 40 ) {
                            res = Elaborate(oo, centroid);
                            double tocka1 = res[0];
                            double tocka2 = res[1];
                            double tocka3 = res[2];
                            double tocka4 = res[3];
                            double tocka5 = res[4];
                            double tocka6 = res[5];

                            double tocka7 = res[6];
                            double tocka8 = res[7];
                            double tocka9 = res[8];
                            double tocka10 = res[9];

                            double e = Angle(new Point(tocka6, (tocka5 - 200)), new Point(tocka4, tocka3), new Point(tocka6, tocka5));
                            double e2 = Angle(new Point(tocka4, tocka3-200), new Point(tocka2, tocka1), new Point(tocka4, tocka3));
                            //Log.d("logic", "cos: " + e);
                                // send the tone to the "alarm" stream (classic beeps go there) with 50% volume
                                if (Math.abs(e) > 0.173648) {
                                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 is duration in ms
                                }
                                if (Math.abs(e2) > 0.5) {
                                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 is duration in ms
                                }


                            //Log.d("logic", "cos: " + e2);
                            // send the tone to the "alarm" stream (classic beeps go there) with 50% volume



                            Imgproc.drawMarker(mRgba, new Point(tocka2, tocka1), new Scalar(0, 255, 0));
                            Imgproc.drawMarker(mRgba, new Point(tocka4, tocka3), new Scalar(0, 255, 0));
                            Imgproc.drawMarker(mRgba, new Point(tocka6, tocka5), new Scalar(0, 255, 0));

                            Imgproc.drawMarker(mRgba, centroid, new Scalar(0, 0, 255)); //Centroid

                            Imgproc.line(mRgba, new Point(tocka2, tocka1), new Point(tocka4, tocka3), new Scalar(0, 255, 0), 4);
                            Imgproc.line(mRgba, new Point(tocka4, tocka3), new Point(tocka6, tocka5), new Scalar(0, 255, 0), 4);
                            Imgproc.drawContours(mRgba, contours, contourIdx, new Scalar(255, 0, 0), 2);

/*
                            double ena = tocka8 - tocka6;
                            double dva = tocka8 - (ena/3);
                            double tri = tocka8 - (4*ena/5);
                            double stiri = tocka8 - (ena/6);

                            Imgproc.drawMarker(mRgba, new Point(tocka6, tocka5), new Scalar(0, 255, 0));
                            Imgproc.drawMarker(mRgba, new Point(tocka8, tocka7), new Scalar(0, 255, 0));

                            Imgproc.drawMarker(mRgba, centroid, new Scalar(0, 0, 255)); //Centroid

                            Imgproc.line(mRgba, new Point(stiri, tocka1), new Point(dva, tocka2), new Scalar(0, 255, 0), 4);
                            Imgproc.line(mRgba, new Point(dva, tocka2), new Point(tri, tocka3), new Scalar(0, 255, 0), 4);


                            //Imgproc.line(mRgba, new Point(100, tocka1), new Point(250, tocka2), new Scalar(0, 255, 0), 4);
                            //Imgproc.line(mRgba, new Point(250, tocka2), new Point(350, tocka3), new Scalar(0, 255, 0), 4);
                            //Imgproc.line(mRgba, new Point(350, tocka3), new Point(470, tocka4), new Scalar(0, 255, 0), 4);
                            Log.d("logic", "(" + tocka5 + ", " + tocka6 + ")");
                            Log.d("logic", "(" + tocka7 + ", " + tocka8 +")");
                            Log.d("logic", "--------------------------------");*/
                            //}
                            //else {
                               // Log.d("logic", "no");
                            //}
                        } catch (Exception e) {
                            Log.d("logic", "1: " + e.toString() +e.getLocalizedMessage() + e.getMessage());
                        }
                //    }});
               // t1.start();
            }

            prvi = 0;
            drugi = 0;
            tretji = 0;
            cetrti = 0;
            minY = 0;
            maxY = 0;
            miY = 99999;
            maY = 0;
            visina = 0;
            najvisjaT = 0;
            srednjaT = 0;
            najnizjaT = 0;
            prviX = 0;
            drugiX = 0;
            tretjiX =0;
            distance = 9999;
            distance2 = 9999;
            distance3 = 9999;

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mRgba;
    }

    public double[]  Elaborate (final Point[] oo, final Point center) {
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                // code goes here.
                try {
/*
                    for (int i = 0; i < oo.length; i++) {
                        if (oo[i].x > center.x) {
                            if (oo[i].x < miY) {
                                miY = oo[i].x;
                                minY = oo[i].y;
                            }

                            if (oo[i].x > maY) {
                                maY = oo[i].x;
                                maxY = oo[i].y;
                            }

                            //TOCKE
                            for (int j = 90; j < 110; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > prvi ) {
                                        prvi = oo[i].y;
                                    }
                                }
                            }

                            for (int j = 240; j < 260; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > drugi ) {
                                        drugi = oo[i].y;
                                    }
                                }
                            }

                            for (int j = 340; j < 360; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > tretji ) {
                                        tretji = oo[i].y;
                                    }
                                }
                            }

                            for (int j = 460; j < 480; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > cetrti ) {
                                        cetrti = oo[i].y;
                                    }
                                }
                            }
                        }
                    }*/

                    for (int i = 0; i < oo.length; i++) {

                        if (oo[i].x < miY) { // najnizja tocka contoura
                            miY = oo[i].x;
                            minY = oo[i].y;
                        }

                        if (oo[i].x > maY) { // najvisja tocka contoura
                            maY = oo[i].x;
                            maxY = oo[i].y;
                        }
                    }


                        visina = (int) (maY - miY);
                        najvisjaT = (int)(maY - (visina/10));
                        srednjaT = (int)( maY - (visina/3));
                        najnizjaT = (int)(maY - (5*visina/6));

                    distance = 9999;
                    distance2 = 9999;
                    distance3 = 9999;

                    for (int i = 0; i < oo.length; i++) {
                        if (oo[i].y > (center.y)) {
                            if (Math.abs(oo[i].x - najvisjaT) < distance) {
                                distance = (Math.abs(oo[i].x - najvisjaT));
                                prvi = oo[i].y;
                                prviX = oo[i].x;
                            }

                            if (Math.abs(oo[i].x - srednjaT) < distance2) {
                                distance2 = (Math.abs(oo[i].x - srednjaT));
                                drugi = oo[i].y;
                                drugiX = oo[i].x;
                            }

                            if (Math.abs(oo[i].x - najnizjaT) < distance3) {
                                distance3 = (Math.abs(oo[i].x - najnizjaT));
                                tretji = oo[i].y;
                                tretjiX = oo[i].x;
                            }
                        }
                    }




/*
                    for (int i = 0; i < oo.length; i++) {

                        if (oo[i].y > (center.y - 50)) {


                            //TOCKE
                            for (int j = najvisjaT-10; j < najvisjaT+10; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > prvi ) {
                                        prvi = oo[i].y;
                                        prviX = oo[i].x;
                                    }
                                }
                            }

                            for (int j = srednjaT-10; j < srednjaT+10; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > drugi ) {
                                        drugi = oo[i].y;
                                        drugiX = oo[i].x;
                                    }
                                }
                            }

                            for (int j = najnizjaT-10; j < najnizjaT+10; j++) {
                                if (oo[i].x == j ) {
                                    if (oo[i].y > tretji ) {
                                        tretji = oo[i].y;
                                        tretjiX = oo[i].x;
                                    }
                                }
                            }
                        }


                    }*/
                    rezultat = new double[]{prvi, prviX, drugi, drugiX, tretji, tretjiX, minY, miY, maxY, maY};
                } catch (Exception e) {
                    Log.d("logic", "2: " + e.toString());
                }


            }});
                 t1.start();
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
}
