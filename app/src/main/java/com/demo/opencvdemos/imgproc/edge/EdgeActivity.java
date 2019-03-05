package com.demo.opencvdemos.imgproc.edge;

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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EdgeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnEdgeSobel, btnEdgeLaplace, btnEdgeCanny;
    ImageView imgSrc, imgDst;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edge);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnEdgeSobel = findViewById(R.id.btnEdgeSobel);
        btnEdgeLaplace = findViewById(R.id.btnEdgeLaplace);
        btnEdgeCanny = findViewById(R.id.btnEdgeCanny);

        btnLoadImg.setOnClickListener(this);
        btnEdgeSobel.setOnClickListener(this);
        btnEdgeLaplace.setOnClickListener(this);
        btnEdgeCanny.setOnClickListener(this);

        imgSrc = findViewById(R.id.imgSrc);
        imgDst = findViewById(R.id.imgDst);
    }

    public void onClick(View v) {
        //图像处理之前需要先加载图片
        if (v.getId() != R.id.btnLoadImg && src == null) {
            Toast.makeText(EdgeActivity.this, "请先加载图片", Toast.LENGTH_SHORT).show();
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
            case R.id.btnEdgeSobel:
                dst = sobel();
                break;
            case R.id.btnEdgeLaplace:
                dst = laplace();
                break;
            case R.id.btnEdgeCanny:
                dst = canny();
                break;
        }

        //显示输出图像
        showDst(dst);
    }

    private Mat sobel() {
        //平滑图像
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(src, blurMat, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        //灰度化图像
        Mat grayMat = new Mat();
        Imgproc.cvtColor(blurMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //Sobel
        Mat grad = new Mat();
        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();
        //Imgproc.Scharr( src_gray, grad_x, ddepth, 1, 0, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayMat, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
        //Imgproc.Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayMat, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0, Core.BORDER_DEFAULT);
        // converting back to CV_8U
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);
        return grad;
    }

    private Mat laplace() {
        //平滑图像
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(src, blurMat, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        //灰度化图像
        Mat grayMat = new Mat();
        Imgproc.cvtColor(blurMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //laplace
        Mat lapMat = new Mat();
        Imgproc.Laplacian(grayMat, lapMat, CvType.CV_16S, 3, 1, 0, Core.BORDER_DEFAULT);
        // converting back to CV_8U
        Mat absLapMat = new Mat();
        Core.convertScaleAbs(lapMat, absLapMat);
        return absLapMat;
    }

    private Mat canny() {
        //平滑图像
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(src, blurMat, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        //灰度化图像
        Mat grayMat = new Mat();
        Imgproc.cvtColor(blurMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //canny
        Mat edgesMat = new Mat();
        Imgproc.Canny(grayMat, edgesMat, 50, 100, 3, false);
        //
        Mat edgesDst = new Mat(src.size(), CvType.CV_8UC3, Scalar.all(0));
        src.copyTo(edgesDst, edgesMat);

        return edgesDst;
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
