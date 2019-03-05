package com.demo.opencvdemos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.demo.opencvdemos.imgproc.blur.BlurActivity;
import com.demo.opencvdemos.imgproc.border.BorderActivity;
import com.demo.opencvdemos.imgproc.draw.DrawActivity;
import com.demo.opencvdemos.imgproc.edge.EdgeActivity;
import com.demo.opencvdemos.imgproc.morphology.MorphologyActivity;
import com.demo.opencvdemos.imgproc.pyramid.PyramidActivity;
import com.demo.opencvdemos.imgproc.threshold.ThresholdActivity;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    Button btnDraw, btnBlur, btnMorphology, btnPyramid,
            btnThreshold, btnBorder, btnEdge,
            btnMat, btnOne, btnTwo, btnThree, btnFour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDraw = findViewById(R.id.btnDraw);
        btnBlur = findViewById(R.id.btnBlur);
        btnMorphology = findViewById(R.id.btnMorphology);
        btnPyramid = findViewById(R.id.btnPyramid);
        btnThreshold = findViewById(R.id.btnThreshold);
        btnBorder = findViewById(R.id.btnBorder);
        btnEdge = findViewById(R.id.btnEdge);

        btnMat = findViewById(R.id.btnMat);
        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);
        btnThree = findViewById(R.id.btnThree);
        btnFour = findViewById(R.id.btnFour);

        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DrawActivity.class));
            }
        });

        btnBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BlurActivity.class));
            }
        });

        btnMorphology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MorphologyActivity.class));
            }
        });

        btnPyramid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PyramidActivity.class));
            }
        });

        btnThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ThresholdActivity.class));
            }
        });

        btnBorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BorderActivity.class));
            }
        });

        btnEdge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EdgeActivity.class));
            }
        });

        btnMat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MatActivity.class));
            }
        });

        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImgFilterActivity.class));
            }
        });

        btnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TwoActivity.class));
            }
        });

        btnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ThreeActivity.class));
            }
        });

        btnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FourActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //使用本地opencv动态库，需要在工程中导入.so动态库，配置好加载路径
        boolean initResult = OpenCVLoader.initDebug();
        Log.d(TAG, "onResume: initResult=" + initResult);

        //使用OpenCV Engine service，需要运行终端事先安装OpenCV Manager
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                if (status == LoaderCallbackInterface.SUCCESS) {
//                    Log.d(TAG, "onManagerConnected: success");
//                } else {
//                    super.onManagerConnected(status);
//                }
//            }
//        });
    }
}
