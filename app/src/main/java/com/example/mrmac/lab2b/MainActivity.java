package com.example.mrmac.lab2b;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.EditText;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;

import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.rectangle;
import static org.opencv.core.Core.split;


public class MainActivity extends ActionBarActivity implements CvCameraViewListener2{
    private CameraBridgeViewBase mOpenCvCameraView;
    int option = 0;
    int Heart_rate;
    //int class_list_index = 0;
    //int faces_needed = 0;
    private Rect forehead_box;
    private boolean record = false;
    private ArrayList<Float> red_array = new ArrayList(0);
    private ArrayList<Float> blue_array = new ArrayList(0);
    private ArrayList<Float> green_array = new ArrayList(0);
    private rgbSig rgbSignals = new rgbSig();

    //private int absoluteFaceSize;
    //private File                   mCascadeFile;
    //private CascadeClassifier cascadeClassifier;
    //public native double extractHR(float* rsig, float* gsig, float* bsig, int nbsamples, double fs);
    private native int getSignalsFromNative(rgbSig obj);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
//                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
//                    current_frame = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }



    private View.OnClickListener trainbtnlistener = new View.OnClickListener(){

        public void onClick(View v){
        record = true;
        //faces_needed = 16;

        }

    };
    private View.OnClickListener testbtnlistener = new View.OnClickListener(){

        public void onClick(View v){

            option = 2;

        }

    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        String libName = "libndk_heartrate.so"; // the module name of the library, without .so
        System.loadLibrary(libName);

        // Size and loc of forehead box
        forehead_box = new Rect(250, 75, 200, 125);
        //
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCVView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        //File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        //mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        //cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());


//        current_frame = new Mat();
//        option = 0; // 0 -> normal 1 -> blur 2 -> edges

        Button trainbtn = (Button)findViewById(R.id.training);
        Button testbtn = (Button)findViewById(R.id.testing);
        //EditText class_name = (EditText)findViewById(R.id.class_name);

        trainbtn.setOnClickListener(trainbtnlistener);
        testbtn.setOnClickListener(testbtnlistener);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        //grayscaleImage = new Mat(height, width, CvType.CV_8UC4);


        // The faces will be a 20% of the height of the screen
        //absoluteFaceSize = (int) (height * 0.2);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Scalar red_mean;
        Scalar green_mean;
        Scalar blue_mean;
        List<Mat> planes = new ArrayList(3);

        Mat current_frame = inputFrame.rgba();

        Mat fore_head = current_frame.submat(forehead_box);

        //Draw Fore head box
        Point pt1 = forehead_box.tl();
        Point pt2 = forehead_box.br();
        Scalar color = new Scalar(255,255,0,0);
        rectangle(current_frame, pt1, pt2, color);

        //Extract the Channels
        if (option == 1) {
            split(fore_head, planes);
            red_mean = mean(planes.get(0));
            green_mean = mean(planes.get(1));
            blue_mean = mean(planes.get(2));
            red_array.add((float) red_mean.val[0]);
            green_array.add((float) green_mean.val[0]);
            blue_array.add((float) blue_mean.val[0]);
        }

        if (option == 2) {
            int len = red_array.size();
            rgbSignals.rsig = new float[len];
            rgbSignals.gsig = new float[len];
            rgbSignals.bsig = new float[len];
            for (int i = 0; i < len; i++) {
                rgbSignals.rsig[i] = red_array.get(i);
                rgbSignals.gsig[i] = green_array.get(i);
                rgbSignals.bsig[i] = blue_array.get(i);
            }
            Heart_rate = getSignalsFromNative(rgbSignals);
            ((android.widget.Button)findViewById(R.id.testing)).setText(Integer.toString(Heart_rate));
            option = 0;
        }


        return current_frame;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_vid, menu);
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
}
