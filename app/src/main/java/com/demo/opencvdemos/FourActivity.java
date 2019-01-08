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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FourActivity extends AppCompatActivity {
    private static final String TAG = "FourActivity";
    private static final int REQ_CODE_PICK_IMG = 1;

    Button btnLoadImg;
    Bitmap originBitmap, currentBitmap;
    ImageView imgView;
    Mat originMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);

        imgView = findViewById(R.id.imgView);

        btnLoadImg = findViewById(R.id.btnLoadImg);

        btnLoadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
                imgPickerIntent.setType("image/*");
                startActivityForResult(imgPickerIntent, REQ_CODE_PICK_IMG);
            }
        });
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

    private void showImg(Bitmap currentBitmap) {
        //显示图像
        imgView.setImageBitmap(currentBitmap);
    }

    private Bitmap rotateBitmap(Bitmap temp, int orientation) {
        Matrix rotateMat = new Matrix();
        rotateMat.postRotate(orientation);
        int width = temp.getWidth();
        int height = temp.getHeight();

        Bitmap bitmap =  Bitmap.createBitmap(temp, 0, 0,
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
