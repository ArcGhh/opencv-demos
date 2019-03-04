package com.demo.opencvdemos.imgproc.blur;

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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BlurActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "BlurActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg,btnBlur,btnBilateralFilter,btnBoxFilter,
            btnGaussianBlur,btnMedianBlur, btnSqrBoxFilter;
    ImageView imgSrc, imgDst;

    Mat src;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnBlur = findViewById(R.id.btnBlur);
        btnBilateralFilter = findViewById(R.id.btnBilateralFilter);
        btnBoxFilter = findViewById(R.id.btnBoxFilter);
        btnGaussianBlur = findViewById(R.id.btnGaussianBlur);
        btnMedianBlur = findViewById(R.id.btnMedianBlur);
        btnSqrBoxFilter = findViewById(R.id.btnSqrBoxFilter);

        btnLoadImg.setOnClickListener(this);
        btnBlur.setOnClickListener(this);
        btnBilateralFilter.setOnClickListener(this);
        btnBoxFilter.setOnClickListener(this);
        btnGaussianBlur.setOnClickListener(this);
        btnMedianBlur.setOnClickListener(this);
        btnSqrBoxFilter.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    @Override
    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null){
            Toast.makeText(BlurActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnBlur:
                //blur 均值模糊
                Imgproc.blur(src, dst, new Size(3, 3));
                break;
            case R.id.btnBilateralFilter:
                Log.d(TAG, "onClick: " + (src.type() == CvType.CV_8UC1 || src.type() == CvType.CV_8UC3));
                /**
                 * bilateralFilter 双边模糊
                 * 需要src和dst都是一通道或者三通道，但是bitmap转mat的方法默认输出4通道，
                 * 所以需要转换，这里先跳过，后续解决双边模糊。
                 */
                //Imgproc.bilateralFilter(src, dst, 5, 130, 130);
                break;
            case R.id.btnBoxFilter:
                //BoxFilter 箱式滤波模糊
                Imgproc.boxFilter(src, dst, -1, new Size(3, 3));
                break;
            case  R.id.btnGaussianBlur:
                //GaussianBlur 高斯模糊
                Imgproc.GaussianBlur(src, dst, new Size(3, 3), 0);
                break;
            case R.id.btnMedianBlur:
                //MedianBlur 中值模糊
                Imgproc.medianBlur(src, dst, 3);
                break;
            case R.id.btnSqrBoxFilter:
                //SqrBoxFilter
                Imgproc.sqrBoxFilter(src, dst, CvType.CV_8U, new Size(3, 3));
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
