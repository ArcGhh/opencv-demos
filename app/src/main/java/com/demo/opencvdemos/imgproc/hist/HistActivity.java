package com.demo.opencvdemos.imgproc.hist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.opencvdemos.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HistActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnHistEqu, btnHistCal, btnHistComp;
    ImageView imgSrc, imgDst, imgSrc1, imgSrc2;
    TextView txvMsg;

    int srcIndex = 0;

    Mat src, src1, src2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hist);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnHistEqu = findViewById(R.id.btnHistEqu);
        btnHistCal = findViewById(R.id.btnHistCal);
        btnHistComp = findViewById(R.id.btnHistComp);

        btnLoadImg.setOnClickListener(this);
        btnHistEqu.setOnClickListener(this);
        btnHistCal.setOnClickListener(this);
        btnHistComp.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgSrc1 = findViewById(R.id.imgSrc1);
        imgSrc2 = findViewById(R.id.imgSrc2);
        imgDst = findViewById(R.id.imgDst);

        imgSrc1.setOnClickListener(this);
        imgSrc2.setOnClickListener(this);

        txvMsg = findViewById(R.id.txvMsg);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(HistActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (v.getId() == R.id.btnLoadImg) {
            srcIndex = 0;
            //加载图片
            loadImg();
            return;
        }

        //初始化输出图像mat
        Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);

        switch (v.getId()) {
            case R.id.btnHistEqu:
                showLessView();
                dst = histEqualize();
                break;
            case R.id.btnHistCal:
                showLessView();
                dst = histCalculation();
                break;
            case R.id.btnHistComp:
                if (src1 == null || src2 == null){
                    showMoreView();
                }else {
                    histComparison();
                }
                break;
            case R.id.imgSrc1:
                srcIndex = 1;
                loadImg();
                break;
            case R.id.imgSrc2:
                srcIndex = 2;
                loadImg();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private void showMoreView(){
        imgDst.setVisibility(View.GONE);
        imgSrc1.setVisibility(View.VISIBLE);
        imgSrc2.setVisibility(View.VISIBLE);
        txvMsg.setVisibility(View.VISIBLE);
    }

    private void showLessView(){
        imgDst.setVisibility(View.VISIBLE);
        imgSrc1.setVisibility(View.GONE);
        imgSrc2.setVisibility(View.GONE);
        txvMsg.setVisibility(View.GONE);
    }

    private Mat histEqualize() {
        //灰度化
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        //直方图均衡
        Mat histEquMat = new Mat();
        Imgproc.equalizeHist(grayMat, histEquMat);
        return histEquMat;
    }

    private Mat histCalculation() {
        //分离源图像颜色面
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        //set bins size
        int histSize = 256;
        //设定像素值范围,不含上界
        float range[] = {0, 256};
        MatOfFloat histRange = new MatOfFloat(range);
        //计算直方图
        Mat bHist = new Mat(), gHist = new Mat(), rHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist,
                new MatOfInt(histSize), histRange, false);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(1), new Mat(), gHist,
                new MatOfInt(histSize), histRange, false);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(2), new Mat(), rHist,
                new MatOfInt(histSize), histRange, false);
        //初始化直方图底图
        int histW = 500, histH = 500;
        int binW = (int) Math.round((double) histW / histSize);
        Mat histImage = new Mat(histH, histW, CvType.CV_8UC3, new Scalar(0, 0, 0));
        //规范化直方图
        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
        //绘制直方图
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
        gHist.get(0, 0, gHistData);
        float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
        rHist.get(0, 0, rHistData);
        for (int i = 1; i < histSize; i++) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
        }
        return histImage;
    }

    private void histComparison(){
        //hsv变换
        Mat hsvBase = new Mat(), hsvTest1 = new Mat(), hsvTest2 = new Mat();
        Imgproc.cvtColor(src, hsvBase, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(src1, hsvTest1, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(src2, hsvTest2, Imgproc.COLOR_BGR2HSV);
        //创建一个src一半的hsv图像
        Mat hsvHalfDown = hsvBase.submat(new Range(hsvBase.rows()/2, hsvBase.rows()-1),
                new Range(0, hsvBase.cols()-1));
        //初始化直方图计算参数
        int hBins = 50, sBins = 60;
        int[] histSize = {hBins, sBins};
        float[] ranges = {0,180,0,256};// 色调：from 0 to 179, 饱和度： from 0 to 255
        int[] channels = {0,1};//使用第0和第1通道
        //计算直方图
        Mat histBase = new Mat(), histHalfDown = new Mat(), histTest1 = new Mat(), histTest2 = new Mat();
        List<Mat> hsvBaseList = Arrays.asList(hsvBase);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), histBase, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvHalfDownList = Arrays.asList(hsvHalfDown);
        Imgproc.calcHist(hsvHalfDownList, new MatOfInt(channels), new Mat(), histHalfDown, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histHalfDown, histHalfDown, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest1List = Arrays.asList(hsvTest1);
        Imgproc.calcHist(hsvTest1List, new MatOfInt(channels), new Mat(), histTest1, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest1, histTest1, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest2List = Arrays.asList(hsvTest2);
        Imgproc.calcHist(hsvTest2List, new MatOfInt(channels), new Mat(), histTest2, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest2, histTest2, 0, 1, Core.NORM_MINMAX);
        //输出对比结果
        StringBuilder stringBuilder = new StringBuilder();
        for( int compareMethod = 0; compareMethod < 4; compareMethod++ ) {
            double baseBase = Imgproc.compareHist( histBase, histBase, compareMethod );
            double baseHalf = Imgproc.compareHist( histBase, histHalfDown, compareMethod );
            double baseTest1 = Imgproc.compareHist( histBase, histTest1, compareMethod );
            double baseTest2 = Imgproc.compareHist( histBase, histTest2, compareMethod );
            String msg = "Method " + compareMethod + " Perfect, Base-Half, Base-Test(1), Base-Test(2) : " + baseBase + " / " + baseHalf
                    + " / " + baseTest1 + " / " + baseTest2 + "\n";
            stringBuilder.append(msg);
        }
        Log.d(TAG, stringBuilder.toString());
        txvMsg.setText(stringBuilder.toString());
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
                    if (srcIndex == 0) {
                        //显示原图
                        imgSrc.setImageBitmap(originImg);
                        //初始化原图mat
                        src = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC3);
                        //将原图bitmap转换为mat，这里src默认转为4通道。。。
                        Utils.bitmapToMat(originImg, src);
                    }else if (srcIndex == 1){
                        imgSrc1.setImageBitmap(originImg);
                        src1 = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC3);
                        Utils.bitmapToMat(originImg, src1);
                    }else if (srcIndex == 2){
                        imgSrc2.setImageBitmap(originImg);
                        src2 = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC3);
                        Utils.bitmapToMat(originImg, src2);
                    }
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
