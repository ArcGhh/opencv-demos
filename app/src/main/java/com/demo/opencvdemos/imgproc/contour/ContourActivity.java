package com.demo.opencvdemos.imgproc.contour;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.opencvdemos.R;
import com.demo.opencvdemos.imgproc.border.BorderActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContourActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnFindContour, btnConvexHull, btnBoundBoxAndCircle,
            btnBoundRotatedBoxAndCircle, btnImgaeMoment;
    ImageView imgSrc, imgDst;
    TextView txvMsg;

    Mat src;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contour);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnFindContour = findViewById(R.id.btnFindContour);
        btnConvexHull = findViewById(R.id.btnConvexHull);
        btnBoundBoxAndCircle = findViewById(R.id.btnBoundBoxAndCircle);
        btnBoundRotatedBoxAndCircle = findViewById(R.id.btnBoundRotatedBoxAndCircle);
        btnImgaeMoment = findViewById(R.id.btnImgaeMoment);

        btnLoadImg.setOnClickListener(this);
        btnFindContour.setOnClickListener(this);
        btnConvexHull.setOnClickListener(this);
        btnBoundBoxAndCircle.setOnClickListener(this);
        btnBoundRotatedBoxAndCircle.setOnClickListener(this);
        btnImgaeMoment.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);

        txvMsg = findViewById(R.id.txvMsg);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(ContourActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnFindContour:
                dst = findContour();
                break;
            case R.id.btnConvexHull:
                dst = convexHull();
                break;
            case R.id.btnBoundBoxAndCircle:
                dst = boundBoxAndCircle();
                break;
            case R.id.btnBoundRotatedBoxAndCircle:
                dst = boundRotatedBoxAndCircle();
                break;
            case R.id.btnImgaeMoment:
                txvMsg.setVisibility(View.VISIBLE);
                dst = imageMoments();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private Mat findContour(){
        //设置参数
        int threshold = 100;
        Random rng = new Random(12345);
        //灰度化
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        //平滑
        Imgproc.blur(gray, gray, new Size(3,3));
        //canny边缘检测
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, threshold, threshold * 2);
        //寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //绘制轮廓
        Mat draw = Mat.zeros(canny.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(draw, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }

        return draw;
    }

    private Mat convexHull(){
        //设置参数
        int threshold = 100;
        Random rng = new Random(12345);
        //灰度化
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        //平滑
        Imgproc.blur(gray, gray, new Size(3,3));
        //canny边缘检测
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, threshold, threshold * 2);
        //寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //计算凸包
        List<MatOfPoint> hullList = new ArrayList<>();
        for (MatOfPoint contour : contours){
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);

            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }
        //绘制边界和凸包
        Mat draw = Mat.zeros(canny.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(draw, contours, i, color);
            Imgproc.drawContours(draw, hullList, i, color );
        }

        return draw;
    }

    private Mat boundBoxAndCircle(){
        //设置参数
        int threshold = 100;
        Random rng = new Random(12345);
        //灰度化和平滑降噪
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(gray, gray, new Size(3,3));
        //canny边缘识别
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, threshold, threshold * 2);
        //寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        //外接矩形和外接圆
        MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
        Rect[] boundRect = new Rect[contours.size()];
        Point[] centers = new Point[contours.size()];
        float[][] radius = new float[contours.size()][1];
        for (int i = 0; i < contours.size(); i++){
            contoursPoly[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
            boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
            centers[i] = new Point();
            Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radius[i]);
        }
        //绘制
        Mat draw = Mat.zeros(canny.size(), CvType.CV_8UC3);
        List<MatOfPoint> contoursPolyList = new ArrayList<>(contoursPoly.length);
        for (MatOfPoint2f poly : contoursPoly){
            contoursPolyList.add(new MatOfPoint(poly.toArray()));
        }
        for (int i = 0; i < contours.size(); i++){
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(draw, contoursPolyList, i, color);
            Imgproc.rectangle(draw, boundRect[i].tl(), boundRect[i].br(), color, 2);
            Imgproc.circle(draw, centers[i], (int)radius[i][0], color, 2);
        }

        return draw;
    }

    private Mat boundRotatedBoxAndCircle(){
        //设置参数
        int threshold = 100;
        Random rng = new Random(12345);
        //灰度化和平滑降噪
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(gray, gray, new Size(3,3));
        //canny边缘识别
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, threshold, threshold * 2);
        //寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        //外接可倾斜矩形/圆
        RotatedRect[] minRect = new RotatedRect[contours.size()];
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];
        for (int i = 0; i < contours.size(); i++){
            minRect[i] = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5){
                minEllipse[i] = Imgproc.fitEllipse(new MatOfPoint2f(contours.get(i).toArray()));
            }
        }
        //绘制
        Mat draw = Mat.zeros(canny.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++){
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            // contour
            Imgproc.drawContours(draw, contours, i, color);
            // ellipse
            Imgproc.ellipse(draw, minEllipse[i], color, 2);
            // rotated rectangle
            Point[] rectPoints = new Point[4];
            minRect[i].points(rectPoints);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(draw, rectPoints[j], rectPoints[(j+1) % 4], color);
            }
        }

        return draw;
    }

    private Mat imageMoments(){
        //设置参数
        int threshold = 100;
        Random rng = new Random(12345);
        //灰度化和平滑降噪
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(gray, gray, new Size(3,3));
        //canny边缘识别
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, threshold, threshold * 2);
        //寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        List<Moments> mu = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++){
            mu.add(Imgproc.moments(contours.get(i)));
        }

        List<Point> mc = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++){
            //add 1e-5 to avoid division by zero
            mc.add(new Point(mu.get(i).m10 / (mu.get(i).m00 + 1e-5), mu.get(i).m01 / (mu.get(i).m00 + 1e-5)));
        }

        Mat draw = Mat.zeros(canny.size(),CvType.CV_8UC3);
        for (int i=0;i<contours.size();i++){
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(draw, contours, i, color, 2);
            Imgproc.circle(draw, mc.get(i), 4, color, -1);
        }

        for (int i = 0; i < contours.size(); i++) {
            System.out.format(" * Contour[%d] - Area (M_00) = %.2f - Area OpenCV: %.2f - Length: %.2f\n", i,
                    mu.get(i).m00, Imgproc.contourArea(contours.get(i)),
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true));
        }

        return draw;
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
