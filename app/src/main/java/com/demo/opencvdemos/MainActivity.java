package com.demo.opencvdemos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    Button btnOne, btnTwo, btnThree, btnFour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);
        btnThree = findViewById(R.id.btnThree);
        btnFour = findViewById(R.id.btnFour);

        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, OneActivity.class));
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
