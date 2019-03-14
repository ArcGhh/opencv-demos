package com.demo.opencvdemos.feature2d.cornerdetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.opencvdemos.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

public class CornerDectctorActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CornerDectctorActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnHarris, btnShiTomasi, btnCustom;
    ImageView imgSrc, imgDst;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corner_detector);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnHarris = findViewById(R.id.btnHarris);
        btnShiTomasi = findViewById(R.id.btnShiTomasi);
        btnCustom = findViewById(R.id.btnCustom);

        btnLoadImg.setOnClickListener(this);
        btnHarris.setOnClickListener(this);
        btnShiTomasi.setOnClickListener(this);
        btnCustom.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(CornerDectctorActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (v.getId() == R.id.btnLoadImg) {
            //加载图片
            loadImg();
            return;
        }

        //初始化输出图像mat
        Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);

        switch (v.getId()) {
            case R.id.btnHarris:
                dst = harris();
                break;
            case R.id.btnShiTomasi:
                dst = shiTomasi();
                break;
            case R.id.btnCustom:
                dst = custom();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private Mat harris() {
        //灰度化
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        //初始化结果mat
        Mat tmp = Mat.zeros(srcGray.size(), CvType.CV_32F);
        //设置参数
        int blockSize = 2;
        int apertureSize = 3;
        double k = 0.04;
        //harris角点检测
        Imgproc.cornerHarris(srcGray, tmp, blockSize, apertureSize, k);
        //规范化结果
        Mat dstNorm = new Mat();
        Mat dstNormScaled = new Mat();
        Core.normalize(tmp, dstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(dstNorm, dstNormScaled);
        //角点处绘制圆圈
        float[] dstNormData = new float[(int) (dstNorm.total() * dstNorm.channels())];
        dstNorm.get(0, 0, dstNormData);
        int threshold = 200;
        for (int i = 0; i < dstNorm.rows(); i++) {
            for (int j = 0; j < dstNorm.cols(); j++) {
                if ((int) dstNormData[i * dstNorm.cols() + j] > threshold) {
                    Imgproc.circle(dstNormScaled, new Point(j, i), 5, new Scalar(0), 2, 8, 0);
                }
            }
        }
        return dstNormScaled;
    }

    private Mat shiTomasi() {
        //灰度化
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        //设置参数
        int maxCorners = 23;
        Random rng = new Random(12345);
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 3, gradientSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;
        //检测角点
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(srcGray, corners, maxCorners, qualityLevel,
                minDistance, new Mat(), blockSize, gradientSize, useHarrisDetector, k);
        //绘制角点
        Log.d(TAG, "shiTomasi number of corners detected: " + corners.rows());
        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);
        int radius = 4;
        Mat copy = src.clone();
        for (int i = 0; i < corners.rows(); i++) {
            Imgproc.circle(copy,
                    new Point(cornersData[i * 2], cornersData[i * 2 + 1]),
                    radius,
                    new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), 255)
                    , Core.FILLED);
        }

        return copy;
    }

    private Mat custom(){
        //灰度化
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        //设置参数
        int blockSize = 3, apertureSize = 3;

        Mat harrisDst = new Mat();
        Imgproc.cornerEigenValsAndVecs(srcGray, harrisDst, blockSize, apertureSize);
        float[] harrisData = new float[(int) (harrisDst.total() * harrisDst.channels())];
        harrisDst.get(0, 0, harrisData);
        Mat Mc = Mat.zeros(srcGray.size(), CvType.CV_32F);
        float[] McData = new float[(int) (Mc.total() * Mc.channels())];
        Mc.get(0, 0, McData);
        for( int i = 0; i < srcGray.rows(); i++ ) {
            for( int j = 0; j < srcGray.cols(); j++ ) {
                float lambda1 = harrisData[(i*srcGray.cols() + j) * 6];
                float lambda2 = harrisData[(i*srcGray.cols() + j) * 6 + 1];
                McData[i*srcGray.cols()+j] = (float) (lambda1*lambda2 - 0.04f*Math.pow( ( lambda1 + lambda2 ), 2 ));
            }
        }
        Mc.put(0, 0, McData);
        double harrisMinVal;
        double harrisMaxVal;
        Core.MinMaxLocResult res = Core.minMaxLoc(Mc);
        harrisMinVal = res.minVal;
        harrisMaxVal = res.maxVal;

        Mat shiTomasiDst = new Mat();
        Imgproc.cornerMinEigenVal(srcGray, shiTomasiDst, blockSize, apertureSize);
        res = Core.minMaxLoc(shiTomasiDst);
        double shiTomasiMinVal;
        double shiTomasiMaxVal;
        shiTomasiMinVal = res.minVal;
        shiTomasiMaxVal = res.maxVal;

        Random rng = new Random(12345);
        int qualityLevel = 50;
        int MAX_QUALITY_LEVEL = 100;
        int qualityLevelVal = Math.max(qualityLevel, 1);
        //Harris
        Mat harrisCopy = new Mat();
        harrisCopy = src.clone();
        float[] McData2 = new float[(int) (Mc.total() * Mc.channels())];
        Mc.get(0, 0, McData2);
        for (int i = 0; i < srcGray.rows(); i++) {
            for (int j = 0; j < srcGray.cols(); j++) {
                if (McData2[i * srcGray.cols() + j] > harrisMinVal
                        + (harrisMaxVal - harrisMinVal) * qualityLevelVal / MAX_QUALITY_LEVEL) {
                    Imgproc.circle(harrisCopy, new Point(j, i), 4,
                            new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
                }
            }
        }
        //Shi-Tomasi
        Mat shiTomasiCopy = new Mat();
        shiTomasiCopy = src.clone();
        float[] shiTomasiData = new float[(int) (shiTomasiDst.total() * shiTomasiDst.channels())];
        shiTomasiDst.get(0, 0, shiTomasiData);
        for (int i = 0; i < srcGray.rows(); i++) {
            for (int j = 0; j < srcGray.cols(); j++) {
                if (shiTomasiData[i * srcGray.cols() + j] > shiTomasiMinVal
                        + (shiTomasiMaxVal - shiTomasiMinVal) * qualityLevelVal / MAX_QUALITY_LEVEL) {
                    Imgproc.circle(shiTomasiCopy, new Point(j, i), 4,
                            new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
                }
            }
        }
        return shiTomasiCopy;
//        return harrisCopy;
    }

    private void loadImg() {
        Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
        imgPickerIntent.setType("image/*");
        startActivityForResult(imgPickerIntent, REQ_CODE_PICK_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    //读取图像到bitmap对象
                    final Uri imgUri = data.getData();
                    final InputStream inputStream = getContentResolver().openInputStream(imgUri);
                    final Bitmap originImg = BitmapFactory.decodeStream(inputStream);
                    //显示原图
                    imgSrc.setImageBitmap(originImg);
                    //初始化原图mat
                    src = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC3);
                    //将原图bitmap转换为mat，这里src默认转为4通道。。。
                    Utils.bitmapToMat(originImg, src);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showDst(Mat dst) {
        showImg(dst, imgDst);
    }

    private void showImg(Mat dst, ImageView imgView) {
        Bitmap processedImg = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, processedImg);
        imgView.setImageBitmap(processedImg);
    }
}
