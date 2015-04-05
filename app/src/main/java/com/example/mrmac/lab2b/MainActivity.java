package com.example.mrmac.lab2b;

import android.content.Context;
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

import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;


public class MainActivity extends ActionBarActivity implements CvCameraViewListener2{
    private CameraBridgeViewBase mOpenCvCameraView;
    int option = 0;
    int class_list_index = 0;
    int faces_needed = 0;
    private ArrayList<String> classes = new ArrayList(0);
    private int absoluteFaceSize;
    private File                   mCascadeFile;
    private CascadeClassifier cascadeClassifier;
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

        faces_needed = 16;

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
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCVView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());


//        current_frame = new Mat();
//        option = 0; // 0 -> normal 1 -> blur 2 -> edges

        Button trainbtn = (Button)findViewById(R.id.training);
        Button testbtn = (Button)findViewById(R.id.testing);
        EditText class_name = (EditText)findViewById(R.id.class_name);

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
        absoluteFaceSize = (int) (height * 0.2);
    }

    public void onCameraViewStopped() {
    }

    public Mat captureImages(Mat aInputFrame) {
        //Hardcode 128,128 image size
        //Pass in path of train images to train and num of images
        //Pass in path of test images to test and num classes
        Mat grayscaleImage = null;
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

        MatOfRect current_face = new MatOfRect();

        // Use the classifier to detect faces
        if (!cascadeClassifier.empty())
            cascadeClassifier.detectMultiScale(grayscaleImage, current_face);

        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = current_face.toArray();
        Mat face = grayscaleImage.submat(facesArray[0]);
        /*for (int i = 0; i <facesArray.length; i++)
            Core.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        */
        return face;
    }



    public void saveToFolder(File file, Mat input){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            Bitmap bmp = Bitmap.createBitmap(input.cols(),  input.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(input,bmp);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // Do signal processing here.
        Mat current_frame = inputFrame.rgba();
        Mat current_face;
        if (faces_needed > 0){
            for(int i=0; i<8; i++){
                //this finds the face and returns a face matrix
                current_face = captureImages(current_frame);
                String file_name = String.format("class_%i/class%i%i", class_list_index, class_list_index, 8 - faces_needed);
                File fileName = new File(Environment.DIRECTORY_PICTURES, file_name);
                saveToFolder(fileName, current_face);
                //now i want to save things to training folder
            }
            faces_needed -= 1;
            if (faces_needed == 0) { // Time to train
                // 1 -> train

            }
            //Below is used to blur the image
            //org.opencv.core.Size s = new Size(10,10);
            //Imgproc.blur(current_frame, current_frame, s);
        } else if (option == 2){
            for(int i=0; i<4; i++) {
                current_face=captureImages(current_frame);
                File fileName=new File(Environment.DIRECTORY_PICTURES, "name it some shit");
                saveToFolder(fileName, current_face);
            }
//          want to test
            //Below does the edge detection
            Imgproc.cvtColor(current_frame,current_frame,Imgproc.COLOR_RGB2GRAY);
            Imgproc.Canny(current_frame,current_frame,100,300);
            Imgproc.cvtColor(current_frame,current_frame,Imgproc.COLOR_GRAY2RGB);

        } else {
            // 0 -> normal
            current_frame = inputFrame.rgba();
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
