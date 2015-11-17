package it.jaschke.alexandria.CameraPreview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import it.jaschke.alexandria.AddBook;
import it.jaschke.alexandria.R;

/**
 * Created by Boram on 2015-11-01.
 */
public class BarcodeScanner extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    private Button scanBtn;
    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    public static final String BARCODE = "barcode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        initControls();
    }

    private void initControls() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(BarcodeScanner.this, mCamera, previewCb,
                autoFocusCb);
        FrameLayout preview =(FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanBtn = (Button)findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeScanned) {
                    barcodeScanned = false;
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCb);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    private void releaseCamera() {
        if(mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        @Override
        public void run() {
            if(previewing)
                mCamera.autoFocus(autoFocusCb);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if(result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for(Symbol sym : syms) {
                    Log.i(BarcodeScanner.class.getSimpleName(),
                            "Barcode: " + sym.getData());
                    String scanResult = sym.getData().trim();

                    Bundle extra = new Bundle();
                    Intent intent = new Intent();

                    extra.putString(BARCODE, scanResult);
                    intent.putExtras(extra);

                    barcodeScanned = true;

                    setResult(AddBook.BARCODE_ACTIVITY, intent);
                    finish();
                    break;
                }
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
