package com.demo.opencvdemos.imgproc.remap;

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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class RemapActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnRemap, btnAffine;
    ImageView imgSrc, imgDst;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remap);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnRemap = findViewById(R.id.btnRemap);
        btnAffine = findViewById(R.id.btnAffine);

        btnLoadImg.setOnClickListener(this);
        btnRemap.setOnClickListener(this);
        btnAffine.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(RemapActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnRemap:
                dst= remap();
                break;
            case R.id.btnAffine:
                dst = affine();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private Mat remap(){
        Mat remapMat = new Mat();
        //映射矩阵
        Mat mapX = new Mat(src.size(), CvType.CV_32F);
        Mat mapY = new Mat(src.size(), CvType.CV_32F);
        float buffX[] = new float[(int) (mapX.total() * mapX.channels())];
        mapX.get(0, 0, buffX);
        float buffY[] = new float[(int) (mapY.total() * mapY.channels())];
        mapY.get(0, 0, buffY);
        for (int i = 0; i < mapX.rows(); i++) {
            for (int j = 0; j < mapX.cols(); j++) {
                buffX[i*mapX.cols() + j] = j;
                buffY[i*mapY.cols() + j] = mapY.rows() - i;
            }
        }
        mapX.put(0,0,buffX);
        mapY.put(0,0,buffY);
        //重映射
        Imgproc.remap(src, remapMat, mapX, mapY, Imgproc.INTER_LINEAR);

        return remapMat;
    }

    private Mat affine(){
        Point[] srcTri = new Point[3];
        srcTri[0] = new Point( 0, 0 );
        srcTri[1] = new Point( src.cols() - 1, 0 );
        srcTri[2] = new Point( 0, src.rows() - 1 );

        Point[] dstTri = new Point[3];
        dstTri[0] = new Point( 0, src.rows()*0.33 );
        dstTri[1] = new Point( src.cols()*0.85, src.rows()*0.25 );
        dstTri[2] = new Point( src.cols()*0.15, src.rows()*0.7 );

        Mat warpMat = Imgproc.getAffineTransform( new MatOfPoint2f(srcTri), new MatOfPoint2f(dstTri) );
        Mat warpDst = Mat.zeros( src.rows(), src.cols(), src.type() );
        Imgproc.warpAffine( src, warpDst, warpMat, warpDst.size() );

        Point center = new Point(warpDst.cols() / 2, warpDst.rows() / 2);
        double angle = -50.0;
        double scale = 0.6;

        Mat rotMat = Imgproc.getRotationMatrix2D( center, angle, scale );
        Mat warpRotateDst = new Mat();
        Imgproc.warpAffine( warpDst, warpRotateDst, rotMat, warpDst.size() );

        return warpRotateDst;
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
