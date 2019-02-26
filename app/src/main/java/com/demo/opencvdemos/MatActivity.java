package com.demo.opencvdemos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MatActivity extends AppCompatActivity {
    private static final String TAG = "MatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat);

        //4行 3列 8位无符号Char型 3通道 元素通道默认值为（0，0，255）
        Mat mat = new Mat(4, 3, CvType.CV_8UC3, new Scalar(0, 0, 255));
        Log.d(TAG, "onCreate: mat" + mat);
    }
}
