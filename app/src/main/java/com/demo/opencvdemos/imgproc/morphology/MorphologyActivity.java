package com.demo.opencvdemos.imgproc.morphology;

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

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MorphologyActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MorphologyActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnDilate, btnErode, btnMorphologyEx,
            btnMorphologyHitOrMiss, btnMorphologyExtHorLine,
            btnMorphologyExtVerLine;
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
        btnMorphologyHitOrMiss = findViewById(R.id.btnMorphologyHitOrMiss);
        btnMorphologyExtHorLine = findViewById(R.id.btnMorphologyExtHorLine);
        btnMorphologyExtVerLine = findViewById(R.id.btnMorphologyExtVerLine);

        btnLoadImg.setOnClickListener(this);
        btnDilate.setOnClickListener(this);
        btnErode.setOnClickListener(this);
        btnMorphologyEx.setOnClickListener(this);

        btnMorphologyHitOrMiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hitMiss();
            }
        });

        btnMorphologyExtHorLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extHorLine();
            }
        });

        btnMorphologyExtVerLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extVerLine();
            }
        });

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    private void hitMiss() {
        Mat input_image = new Mat(8, 8, CvType.CV_8UC1);
        int row = 0, col = 0;
        input_image.put(row, col,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 255, 255, 255, 0, 0, 0, 255,
                0, 255, 255, 255, 0, 0, 0, 0,
                0, 255, 255, 255, 0, 255, 0, 0,
                0, 0, 255, 0, 0, 0, 0, 0,
                0, 0, 255, 0, 0, 255, 255, 0,
                0, 255, 0, 255, 0, 0, 255, 0,
                0, 255, 255, 255, 0, 0, 0, 0);

        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(row, col,
                0, 1, 0,
                1, -1, 1,
                0, 1, 0);

        Mat output_image = new Mat();
        Imgproc.morphologyEx(input_image, output_image, Imgproc.MORPH_HITMISS, kernel);

        showImg(input_image, imgSrc);
        showDst(output_image);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(MorphologyActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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

    private void extHorLine() {
        if (src == null) {
            Toast.makeText(MorphologyActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
            return;
        }
        //灰度化图像
        Mat gray = new Mat();
        if (src.channels() == 3) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = src;
        }
        //分离通道
        List<Mat> channelMat = new ArrayList<>();
        Core.split(gray, channelMat);
        //取某一通道作为1-通道灰度图像
        Mat graySingle = channelMat.get(0);
        //二值化图像
        Mat binary = new Mat();
        Core.bitwise_not(graySingle, graySingle);
        Imgproc.adaptiveThreshold(graySingle, binary, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                15, -2);
        //提取横线
        Mat horizontal = binary.clone();//待提取横线图像
        int horizontalSize = horizontal.cols() / 30;//横线尺寸阈值
        Mat horizontalStructure =
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                        new Size(horizontalSize, 1));//结构元素
        Imgproc.erode(horizontal, horizontal, horizontalStructure);//腐蚀
        Imgproc.dilate(horizontal, horizontal, horizontalStructure);//膨胀
        //显示提取结果
        showImg(horizontal, imgDst);
    }


    private void extVerLine() {
        if (src == null) {
            Toast.makeText(MorphologyActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
            return;
        }
        //灰度化图像
        Mat gray = new Mat();
        if (src.channels() == 3) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = src;
        }
        //分离通道
        List<Mat> channelMat = new ArrayList<>();
        Core.split(gray, channelMat);
        //取某一通道作为1-通道灰度图像
        Mat graySingle = channelMat.get(0);
        //二值化图像
        Mat binary = new Mat();
        Core.bitwise_not(graySingle, graySingle);
        Imgproc.adaptiveThreshold(graySingle, binary, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                15, -2);
        //提取竖线
        Mat vertical = binary.clone();//待提取竖线图像
        int verticalSize = vertical.rows() / 30;//竖线尺寸阈值
        Mat verticalStructure =
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                        new Size(1, verticalSize));//结构元素
        Imgproc.erode(vertical, vertical, verticalStructure);//腐蚀
        Imgproc.dilate(vertical, vertical, verticalStructure);//膨胀
        //显示提取结果
        showImg(vertical, imgDst);
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
