package com.demo.opencvdemos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class TwoActivity extends AppCompatActivity {

    private static final String TAG = "TwoActivity";
    private static final int VIEW_MODE_RGBA = 0;
    private static final int VIEW_MODE_GRAY = 1;
    private static final int VIEW_MODE_CANNY = 2;
    private static final int VIEW_MODE_FEATURE = 3;

    private CameraBridgeViewBase cameraBridgeViewBase;
    private int viewMode = VIEW_MODE_GRAY;
    private Mat matGray, matRgba, matCanny;
    private Button btnGray, btnRGBA, btnCanny, btnFeature;

    private native void findFeatures(long matAddrGray, long matAddrRgba);

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);

        btnGray = findViewById(R.id.btnGray);
        btnRGBA = findViewById(R.id.btnRGBA);
        btnCanny = findViewById(R.id.btnCanny);
        btnFeature = findViewById(R.id.btnFeature);

        btnGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = VIEW_MODE_GRAY;
            }
        });

        btnRGBA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = VIEW_MODE_RGBA;
            }
        });

        btnCanny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = VIEW_MODE_CANNY;
            }
        });

        btnFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = VIEW_MODE_FEATURE;
            }
        });

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.ocvView);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "onCameraViewStarted: width=" + width + ";height=" + height);
                matGray = new Mat(height, width, CvType.CV_8UC1);
                matRgba = new Mat(height, width, CvType.CV_8UC4);
                matCanny = new Mat(height, width, CvType.CV_8UC4);
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "onCameraViewStopped: ocv stopped");
                matGray.release();
                matRgba.release();
                matCanny.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                switch (viewMode) {
                    case VIEW_MODE_GRAY:
                        Imgproc.cvtColor(inputFrame.gray(), matRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                        break;
                    case VIEW_MODE_RGBA:
                        matRgba = inputFrame.rgba();
                        break;
                    case VIEW_MODE_CANNY:
                        matRgba = inputFrame.rgba();
                        Imgproc.Canny(inputFrame.gray(), matCanny, 80, 100);
                        Imgproc.cvtColor(matCanny, matRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                        break;
                    case VIEW_MODE_FEATURE:
                        matRgba = inputFrame.rgba();
                        matGray = inputFrame.gray();
                        findFeatures(matGray.getNativeObjAddr(), matRgba.getNativeObjAddr());
                        break;
                }

                return matRgba;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }
}
