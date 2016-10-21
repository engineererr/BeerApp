package ch.kurky.beerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class QrActivity extends AppCompatActivity {
    QREader qrEader;
    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private SurfaceView surfaceView;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        surfaceView = (SurfaceView) findViewById(R.id.camera_view);

        // handler to get result from QR reader
        mHandler = new Handler(Looper.getMainLooper()) {
            /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                String data = inputMessage.getData().getString("data");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("qrdata", data);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        };


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else{
            qrEader = new QREader.Builder(this, surfaceView, new QRDataListener() {
                @Override public void onDetected(final String data) {
                    Log.d("QREader", "Value : " + data);
                    Bundle bundle = new Bundle();
                    bundle.putString("data", data);
                    Message msg = mHandler.obtainMessage();
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            }).build();

            qrEader.init();
            qrEader.start();

        }
    }

    @Override
    public void onStop(){
        super.onStop();
        qrEader.stop();
        qrEader.releaseAndCleanup();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    qrEader = new QREader.Builder(this, surfaceView, new QRDataListener() {
                        @Override public void onDetected(final String data) {
                            // Do something with the string data
                            Log.d("QREader", "Value : " + data);
                            Bundle bundle = new Bundle();
                            bundle.putString("data", data);
                            Message msg = mHandler.obtainMessage();
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    }).build();
                    surfaceView.postInvalidate();
                    qrEader.init();
                    qrEader.start();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
