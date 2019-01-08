package com.demo.opencvdemos;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by wangyt on 2019/1/7
 */
public class MyView extends JavaCameraView implements Camera.PictureCallback{

    private static final String TAG = "MyView";
    private String picFileName;

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList(){
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported(){
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect(){
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setColorEffect(effect);
        mCamera.setParameters(parameters);
    }

    public List<Camera.Size> getResolutionList(){
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution){
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution(){
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName){
        Log.d(TAG, "takePicture: ");
        this.picFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken: ");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        try {
            FileOutputStream fos = new FileOutputStream(picFileName);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "onPictureTaken: ", e);
        }
    }
}
