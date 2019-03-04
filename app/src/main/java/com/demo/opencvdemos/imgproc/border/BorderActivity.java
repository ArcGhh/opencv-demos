package com.demo.opencvdemos.imgproc.border;

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
import android.widget.Toast;

import com.demo.opencvdemos.R;
import com.demo.opencvdemos.imgproc.pyramid.PyramidActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BorderActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnBorderConstant, btnBorderReplicate;
    ImageView imgSrc, imgDst;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_border);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnBorderConstant = findViewById(R.id.btnBorderConstant);
        btnBorderReplicate = findViewById(R.id.btnBorderReplicate);

        btnLoadImg.setOnClickListener(this);
        btnBorderConstant.setOnClickListener(this);
        btnBorderReplicate.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }


    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(BorderActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnBorderConstant:
                Core.copyMakeBorder(src, dst,
                        30, 30, 30, 30,
                        Core.BORDER_CONSTANT
                        , new Scalar(123, 123, 123));
                break;
            case R.id.btnBorderReplicate:
                Core.copyMakeBorder(src, dst,
                        30, 30, 30, 30,
                        Core.BORDER_REFLECT);
                break;
        }

        //显示输出图像
        showDst(dst);
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
