package com.demo.opencvdemos;

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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class OneActivity extends AppCompatActivity {
    private static final String TAG = "OneActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg, btnBlurImg, btnBlurImgGS, btnBlurImgMid,
            btnSharpImg, btnDilateImg, btnErodImg, btnThodImg,
            btnThodAdaImg;
    ImageView imgOrigin, imgProcess;

    Mat src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        btnLoadImg = findViewById(R.id.btnLoadImg);
        btnBlurImg = findViewById(R.id.btnBlurImg);
        btnBlurImgGS = findViewById(R.id.btnBlurImgGS);
        btnBlurImgMid = findViewById(R.id.btnBlurImgMid);
        btnSharpImg = findViewById(R.id.btnSharpImg);
        btnDilateImg = findViewById(R.id.btnDilateImg);
        btnErodImg = findViewById(R.id.btnErodImg);
        btnThodImg = findViewById(R.id.btnThodImg);
        btnThodAdaImg = findViewById(R.id.btnThodAdaImg);

        imgOrigin = findViewById(R.id.imgOrigin);
        imgProcess = findViewById(R.id.imgProcess);

        btnLoadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
                imgPickerIntent.setType("image/*");
                startActivityForResult(imgPickerIntent, REQ_CODE_PICK_IMG);
            }
        });

        btnBlurImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }
                Imgproc.blur(src, src, new Size(3, 3));
                showMat();
            }
        });

        btnBlurImgGS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Imgproc.GaussianBlur(src, src, new Size(3, 3), 0);
                showMat();
            }
        });

        btnBlurImgMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Imgproc.medianBlur(src, src, 3);
                showMat();
            }
        });

        btnSharpImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Mat kernel = new Mat(3, 3, CvType.CV_16SC1);
                kernel.put(0, 0,
                        0, -1, 0,
                        -1, 5, -1,
                        0, -1, 0);
                Imgproc.filter2D(src, src, src.depth(), kernel);
                showMat();
            }
        });

        btnDilateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.dilate(src, src, kernel);
                showMat();
            }
        });

        btnErodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
                Imgproc.erode(src, src, kernel);
                showMat();
            }
        });

        btnThodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY);
                showMat();
            }
        });

        btnThodAdaImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (src == null) {
                    Toast.makeText(OneActivity.this, "src = null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
                Imgproc.adaptiveThreshold(src, src, 255,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,
                        3, 0);
                showMat();
            }
        });
    }

    private void showMat() {
        Bitmap processedImg = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, processedImg);
        imgProcess.setImageBitmap(processedImg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imgUri = data.getData();
                    final InputStream inputStream = getContentResolver().openInputStream(imgUri);
                    final Bitmap originImg = BitmapFactory.decodeStream(inputStream);
                    imgOrigin.setImageBitmap(originImg);
                    src = new Mat(originImg.getHeight(), originImg.getWidth(), CvType.CV_8UC4);
                    Utils.bitmapToMat(originImg, src);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //使用本地opencv动态库，需要在工程中导入.so动态库，配置好加载路径
        boolean initResult = OpenCVLoader.initDebug();
        Log.d(TAG, "onResume: initResult=" + initResult);

        //使用OpenCV Engine service，需要运行终端事先安装OpenCV Manager
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    Log.d(TAG, "onManagerConnected: success");
                } else {
                    super.onManagerConnected(status);
                }
            }
        });
    }
}
