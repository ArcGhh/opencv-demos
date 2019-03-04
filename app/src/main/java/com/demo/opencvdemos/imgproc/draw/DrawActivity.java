package com.demo.opencvdemos.imgproc.draw;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.demo.opencvdemos.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnDrawLine, btnDrawEllipse, btnDrawCircle, btnDrawPolygon,
            btnDrawRect;
    ImageView imgDst;

    Mat mat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mat = Mat.zeros(500, 500, CvType.CV_8UC3);

        imgDst = findViewById(R.id.imgDst);

        btnDrawLine = findViewById(R.id.btnDrawLine);
        btnDrawEllipse = findViewById(R.id.btnDrawEllipse);
        btnDrawCircle = findViewById(R.id.btnDrawCircle);
        btnDrawPolygon = findViewById(R.id.btnDrawPolygon);
        btnDrawRect = findViewById(R.id.btnDrawRect);

        btnDrawLine.setOnClickListener(this);
        btnDrawEllipse.setOnClickListener(this);
        btnDrawCircle.setOnClickListener(this);
        btnDrawPolygon.setOnClickListener(this);
        btnDrawRect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDrawLine:
                line(new Point(0, 150), new Point(200, 450));
                break;
            case R.id.btnDrawEllipse:
                ellipse(30);
                break;
            case R.id.btnDrawCircle:
                circle(new Point(150,150));
                break;
            case R.id.btnDrawPolygon:
                polygon();
                break;
            case R.id.btnDrawRect:
                rect();
                break;
        }
        showDst(mat);
    }

    private void line(Point start, Point end) {
        int thickness = 2;
        int lineType = 8;
        int shift = 0;
        //源图像，线段起始点，线段终点，线颜色，线宽，线型，点坐标的小数位数
        Imgproc.line(mat, start, end, new Scalar(255, 0, 0), thickness, lineType, shift);
    }

    private void ellipse(double angle) {
        int thickness = 2;
        int lineType = 8;
        int shift = 0;
        //源图像，椭圆圆心，椭圆主轴半径（长轴、短轴 的一半），椭圆旋转角度（顺时针）
        //开始绘制椭圆圆弧的角度，结束绘制椭圆圆弧的角度，线颜色，线宽，线型
        //点坐标的小数位数
        Imgproc.ellipse(mat, new Point(200, 300),
                new Size(100, 60),
                angle, 0.0, 360.0,
                new Scalar(255, 0, 0), thickness, lineType, shift);
    }

    private void circle(Point center){
        int thickness = -1; //-1 表示绘制填充圆
        int lineType = 8;
        int shift = 0;
        //源图像，圆心，半径，颜色，线宽，线型，点坐标的小数位数
        Imgproc.circle(mat, center, 55, new Scalar(0,0,255), thickness, lineType, shift);
    }

    private void polygon(){
        int lineType = 8;
        int shift = 0;

        Point[] points = new Point[5];
        points[0] = new Point(10, 10);
        points[1] = new Point(10, 70);
        points[2] = new Point(50, 90);
        points[3] = new Point(60, 150);
        points[4] = new Point(30, 200);

        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromArray(points);

        List<MatOfPoint> matOfPoints = new ArrayList<>();
        matOfPoints.add(matOfPoint);

        Imgproc.fillPoly(mat, matOfPoints, new Scalar(0,255,0), lineType,shift,new Point(0,0));
    }

    private void rect(){
        Imgproc.rectangle(mat, new Point(80,80),new Point(300, 300),
                new Scalar(134, 210, 35), -1, 8, 0);
    }

    private void showDst(Mat dst) {
        Bitmap processedImg = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, processedImg);
        imgDst.setImageBitmap(processedImg);
    }
}
