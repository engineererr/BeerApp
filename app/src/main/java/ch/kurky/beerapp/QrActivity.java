package ch.kurky.beerapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class QrActivity extends AppCompatActivity {
    QREader qrReader;
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

        qrReader = new QREader.Builder(this, surfaceView, new QRDataListener() {
            @Override public void onDetected(final String data) {
                Log.d("QREader", "Value : " + data);
                Bundle bundle = new Bundle();
                bundle.putString("data", data);
                Message msg = mHandler.obtainMessage();
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }).build();
        qrReader.init();
    }

    @Override protected void onStart() {
        super.onStart();
        qrReader.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        qrReader.stop();
        qrReader.releaseAndCleanup();
    }
}
