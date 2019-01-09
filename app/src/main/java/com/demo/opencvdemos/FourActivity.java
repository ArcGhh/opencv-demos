package com.demo.opencvdemos;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FourActivity extends AppCompatActivity {
    private static final String TAG = "FourActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnDog, btnCanny, btnSobel, btnHarris,
            btnHoughLineP, btnHoughCircle, btnContour;
    Bitmap originBitmap, currentBitmap;
    ImageView imgView, imgDoG;
    Mat originMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);

        imgView = findViewById(R.id.imgView);
        imgDoG = findViewById(R.id.imgDoG);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnDog = findViewById(R.id.btnDog);
        btnCanny = findViewById(R.id.btnCanny);
        btnSobel = findViewById(R.id.btnSobel);
        btnHarris = findViewById(R.id.btnHarris);
        btnHoughLineP = findViewById(R.id.btnHoughLineP);
        btnHoughCircle = findViewById(R.id.btnHoughCircle);
        btnContour = findViewById(R.id.btnContour);

        btnLoadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
                imgPickerIntent.setType("image/*");
                startActivityForResult(imgPickerIntent, REQ_CODE_PICK_IMG);
            }
        });

        btnDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diffOfGaus();
                showImg(currentBitmap);
            }
        });

        btnCanny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canny();
                showImg(currentBitmap);
            }
        });

        btnSobel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sobel();
                showImg(currentBitmap);
            }
        });

        btnHarris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                harris();
                showImg(currentBitmap);
            }
        });

        btnHoughLineP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                houghLineP();
                showImg(currentBitmap);
            }
        });

        btnHoughCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                houghCircle();
                showImg(currentBitmap);
            }
        });

        btnContour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contour();
                showImg(currentBitmap);
            }
        });
    }

    private void contour() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat cannyEdgeMat = new Mat();
        Mat hierarchy = new Mat();
        //轮廓列表
        List<MatOfPoint> contourList = new ArrayList<>();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdgeMat, 10, 100);
        //提取轮廓
        Imgproc.findContours(cannyEdgeMat, contourList, hierarchy,
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        //绘制
        Mat contoursMat = new Mat();
        contoursMat.create(cannyEdgeMat.rows(), cannyEdgeMat.cols(), CvType.CV_8UC3);
        Random random = new Random();
        for (int i = 0; i < contourList.size(); i++) {
            Imgproc.drawContours(contoursMat, contourList, i,
                    new Scalar(random.nextInt(255), random.nextInt(255), random.nextInt(255))
                    , -1);
        }
        Utils.matToBitmap(contoursMat, currentBitmap);
    }

    private void houghCircle() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat cannyEdgeMat = new Mat();
        Mat circleMat = new Mat();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //canny
        Imgproc.Canny(grayMat, cannyEdgeMat, 10, 100);
        //HoughLinesP
        Imgproc.HoughCircles(cannyEdgeMat, circleMat, Imgproc.CV_HOUGH_GRADIENT,
                1, cannyEdgeMat.rows() / 15);

        Mat houghCircleMat = new Mat();
        houghCircleMat.create(cannyEdgeMat.rows(), cannyEdgeMat.cols(), CvType.CV_8UC1);
        //绘制
        for (int i = 0; i < circleMat.cols(); i++) {
            for (int j = 0; j < circleMat.rows(); j++) {
                double[] params = circleMat.get(j, i);
                double x, y;
                int r;
                x = params[0];
                y = params[1];
                r = (int) params[2];
                Point center = new Point(x, y);
                Imgproc.circle(houghCircleMat, center, r, new Scalar(255, 0, 0), 1);
            }
        }
        //mat转为位图
        Utils.matToBitmap(houghCircleMat, currentBitmap);
    }

    private void houghLineP() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat cannyEdgeMat = new Mat();
        Mat lineMat = new Mat();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //canny
        Imgproc.Canny(grayMat, cannyEdgeMat, 10, 100);
        //HoughLinesP
        Imgproc.HoughLinesP(cannyEdgeMat, lineMat, 1, Math.PI / 180, 50, 20, 20);

        Mat houghLineMat = new Mat();
        houghLineMat.create(cannyEdgeMat.rows(), cannyEdgeMat.cols(), CvType.CV_8UC1);
        //绘制
        for (int i = 0; i < lineMat.cols(); i++) {
            for (int j = 0; j < lineMat.rows(); j++) {
                double[] poeints = lineMat.get(j, i);
                double x1, y1, x2, y2;
                x1 = poeints[0];
                y1 = poeints[1];
                x2 = poeints[2];
                y2 = poeints[3];
                Point point1 = new Point(x1, y1);
                Point point2 = new Point(x2, y2);
                Imgproc.line(houghLineMat, point1, point2, new Scalar(255, 0, 0), 1);
            }
        }
        //mat转为位图
        Utils.matToBitmap(houghLineMat, currentBitmap);
    }

    private void harris() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat cornersMat = new Mat();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat tempDst = new Mat();
        //识别角点
        Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
        //归一化角点输出
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, cornersMat);
        //绘制角点
        Random random = new Random();
        for (int i = 0; i < tempDstNorm.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 150) {
                    Imgproc.circle(cornersMat, new Point(i, j),
                            5, new Scalar(random.nextInt(255), 2));
                }
            }
        }
        //mat转换为位图
        Utils.matToBitmap(cornersMat, currentBitmap);
    }

    private void sobel() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat sobelMat = new Mat();
        Mat gradXMat = new Mat();
        Mat absGradXMat = new Mat();
        Mat gradYMat = new Mat();
        Mat absGradYMat = new Mat();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //计算水平、垂直方向梯度
        Imgproc.Sobel(grayMat, gradXMat, CvType.CV_16S,
                1, 0, 3, 1, 0);
        Imgproc.Sobel(grayMat, gradYMat, CvType.CV_16S,
                0, 1, 3, 1, 0);
        //计算梯度绝对值
        Core.convertScaleAbs(gradXMat, absGradXMat);
        Core.convertScaleAbs(gradYMat, absGradYMat);
        //计算结果梯度
        Core.addWeighted(absGradXMat, 0.5, absGradYMat, 0.55, 1, sobelMat);
        //mat转为位图
        Utils.matToBitmap(sobelMat, currentBitmap);
    }

    private void canny() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat edgeMat = new Mat();
        //灰度化
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //canny
        Imgproc.Canny(grayMat, edgeMat, 10, 100);
        //Mat转为位图
        Utils.matToBitmap(edgeMat, currentBitmap);
    }

    private void diffOfGaus() {
        if (originMat == null) {
            Toast.makeText(this, "originMat = null", Toast.LENGTH_SHORT).show();
            return;
        }
        Mat grayMat = new Mat();
        Mat blur1 = new Mat();
        Mat blur2 = new Mat();
        Mat dogMat = new Mat();
        //将图像转化为灰度
        Imgproc.cvtColor(originMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //采用不同的模糊半径做模糊处理
        Imgproc.GaussianBlur(grayMat, blur1, new Size(15, 15), 5);
        Imgproc.GaussianBlur(grayMat, blur2, new Size(21, 21), 5);
        //模糊后的两张图像相减
        Core.absdiff(blur1, blur2, dogMat);
        //反转二值阈值化
        Core.multiply(dogMat, new Scalar(100), dogMat);
        Imgproc.threshold(dogMat, dogMat, 50, 255, Imgproc.THRESH_BINARY_INV);
        //将mat转换为位图
        Utils.matToBitmap(dogMat, currentBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMG) {
            if (resultCode == RESULT_OK) {
                String picturePath = getPicturePath(data);
                //重采样加载，提高速度
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap temp = BitmapFactory.decodeFile(picturePath);
                //获取图像方向信息
                int orientation = 0;
                try {
                    ExifInterface imgParams = new ExifInterface(picturePath);
                    orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    Log.d(TAG, "onActivityResult: orientation=" + orientation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //旋转图像，得到正确图像
                originBitmap = rotateBitmap(temp, orientation);
                //将位图转换为Mat
                Bitmap tempBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
                originMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
                Utils.bitmapToMat(tempBitmap, originMat);

                currentBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, false);
                showImg(currentBitmap);
            }
        }
    }

    private void showImg(Bitmap bitmap) {
        //显示图像
        imgView.setImageBitmap(bitmap);
    }

    private Bitmap rotateBitmap(Bitmap temp, int orientation) {
        Matrix rotateMat = new Matrix();
        rotateMat.postRotate(orientation);
        int width = temp.getWidth();
        int height = temp.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(temp, 0, 0,
                width, height, rotateMat, false);
        return bitmap;
    }

    private String getPicturePath(Intent data) {
        Uri imgUri = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imgUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }
}
