package com.demo.opencvdemos;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class ThreeActivity extends AppCompatActivity {

    private static final String TAG = "ThreeActivity";

    private MyView myView;
    private List<Camera.Size> resolutionList;

    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three);
        myView = findViewById(R.id.myView);
        myView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "onCameraViewStarted: width=" + width + ";height=" + height);
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "onCameraViewStopped: ");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                return inputFrame.rgba();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myView != null) {
            myView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myView != null) {
            myView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myView.enableView();
        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                savePicture();
                return false;
            }
        });
    }

    private void savePicture() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        myView.takePicture(fileName);
        Log.d(TAG, "onTouch: " + fileName + "saved");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = myView.getEffectList();
        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];
        for (int i = 0; i < effects.size(); i++) {
            mEffectMenuItems[i] = mColorEffectsMenu.add(1, i, Menu.NONE, effects.get(i));
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        resolutionList = myView.getResolutionList();
        mResolutionMenuItems = new MenuItem[resolutionList.size()];
        for (int i = 0; i < resolutionList.size(); i++) {
            String itemName = Integer.valueOf(resolutionList.get(i).width).toString() + "x" + Integer.valueOf(resolutionList.get(i).height).toString();
            mResolutionMenuItems[i] = mResolutionMenu.add(2, i, Menu.NONE, itemName);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (item.getGroupId() == 1) {
            myView.setEffect((String) item.getTitle());
            Toast.makeText(this, myView.getEffect(), Toast.LENGTH_SHORT).show();
        } else if (item.getGroupId() == 2) {
            int id = item.getItemId();
            Camera.Size resolution = resolutionList.get(id);
            myView.setResolution(resolution);
            resolution = myView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
