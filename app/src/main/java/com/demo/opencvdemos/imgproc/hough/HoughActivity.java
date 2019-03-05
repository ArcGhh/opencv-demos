package com.demo.opencvdemos.imgproc.hough;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.opencvdemos.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class HoughActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnHoughLines, btnHoughLinesP, btnHoughCircles;
    ImageView imgSrc, imgDst;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hough);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnHoughLines = findViewById(R.id.btnHoughLines);
        btnHoughLinesP = findViewById(R.id.btnHoughLinesP);
        btnHoughCircles = findViewById(R.id.btnHoughCircles);

        btnLoadImg.setOnClickListener(this);
        btnHoughLines.setOnClickListener(this);
        btnHoughLinesP.setOnClickListener(this);
        btnHoughCircles.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(HoughActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnHoughLines:
                dst = houghLines();
                break;
            case R.id.btnHoughLinesP:
                dst = houghLinesP();
                break;
            case R.id.btnHoughCircles:
                dst = houghCircles();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private Mat houghLines() {
        Mat cdst = new Mat();
        //边缘检测
        Mat edgesMat = new Mat();
        Imgproc.Canny(src, edgesMat, 50, 200, 3, false);
        // Copy edges to the images that will display the results in BGR
        Imgproc.cvtColor(edgesMat, cdst, Imgproc.COLOR_GRAY2BGR);
        //标准霍夫线变换
        Mat linesMat = new Mat();
        Imgproc.HoughLines(edgesMat, linesMat, 1, Math.PI / 180, 150);
        //绘制线
        for (int x = 0; x < linesMat.rows(); x++) {
            double rho = linesMat.get(x, 0)[0],
                    theta = linesMat.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
            Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
        return cdst;
    }

    private Mat houghLinesP() {
        Mat cdstP = new Mat();
        //边缘检测
        Mat edgesMat = new Mat();
        Imgproc.Canny(src, edgesMat, 50, 200, 3, false);
        // Copy edges to the images that will display the results in BGR
        Imgproc.cvtColor(edgesMat, cdstP, Imgproc.COLOR_GRAY2BGR);
        //统计概率霍夫线变换
        Mat linesMat = new Mat();
        Imgproc.HoughLinesP(edgesMat, linesMat, 1, Math.PI / 180, 50, 50, 10);
        //绘制线
        for (int x = 0; x < linesMat.rows(); x++) {
            double[] line = linesMat.get(x, 0);
            Imgproc.line(cdstP,
                    new Point(line[0], line[1]), new Point(line[2], line[3]),
                    new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
        return cdstP;
    }

    private Mat houghCircles(){
//        Mat cdst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3, new Scalar(0,0,0));
        Mat cdst = src.clone();
        //图像灰度化
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_RGB2GRAY);
        //中值模糊
        Imgproc.medianBlur(grayMat, grayMat, 5);
        //霍夫圆变换
        Mat circlesMat = new Mat();
        Imgproc.HoughCircles(grayMat, circlesMat, Imgproc.HOUGH_GRADIENT,
                1.0, (double)grayMat.rows()/16,
                100.0, 30.0, 1, 30);
        //绘制
        for (int x = 0; x < circlesMat.cols(); x++){
            double[] circle = circlesMat.get(0,x);
            Point center = new Point(Math.round(circle[0]), Math.round(circle[1]));
            //绘制圆心
            Imgproc.circle(cdst, center, 1, new Scalar(0,0,255,255),3,8,0);
            //绘制圆边框
            int radius = (int) Math.round(circle[2]);
            Imgproc.circle(cdst, center, radius, new Scalar(0,0,255,255),3,8,0);
        }
        return cdst;
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
