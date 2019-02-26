package com.demo.opencvdemos.imgfliter.morphology;

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
import com.demo.opencvdemos.imgfliter.blur.BlurActivity;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MorphologyActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MorphologyActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnDilate, btnErode, btnMorphologyEx;
    ImageView imgSrc, imgDst;

    Mat src;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morphology);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnDilate = findViewById(R.id.btnDilate);
        btnErode = findViewById(R.id.btnErode);
        btnMorphologyEx = findViewById(R.id.btnMorphologyEx);

        btnLoadImg.setOnClickListener(this);
        btnDilate.setOnClickListener(this);
        btnErode.setOnClickListener(this);
        btnMorphologyEx.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null){
            Toast.makeText(MorphologyActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (v.getId() == R.id.btnLoadImg){
            //加载图片
            loadImg();
            return;
        }

        //初始化输出图像mat
        Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);

        switch (v.getId()){
            case R.id.btnDilate:
                //创建kernel
                Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                //膨胀图像
                Imgproc.dilate(src, dst, dilateKernel);
                break;
            case R.id.btnErode:
                //创建kernel
                Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
                //腐蚀图像
                Imgproc.erode(src, dst, erodeKernel);
                break;
            case R.id.btnMorphologyEx:
                //创建kernel
                Mat openKernel = Imgproc.getStructuringElement(Imgproc.MORPH_OPEN, new Size(15, 15));
                //形态学-开运算
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_OPEN, openKernel);
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
        Bitmap processedImg = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, processedImg);
        imgDst.setImageBitmap(processedImg);
    }
}
