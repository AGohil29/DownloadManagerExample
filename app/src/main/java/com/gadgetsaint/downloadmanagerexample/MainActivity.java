package com.gadgetsaint.downloadmanagerexample;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnDownload, btnCancel;
    private TextView txtStatus;
    private volatile boolean done;
    private long downloadId;
    private DownloadManager downloadManager;
    private Handler handlerUpdate;

    private Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            if (done)
                return;
            MainActivity.this.updateStatus();
            handlerUpdate.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        done = false;

        btnDownload = (Button) findViewById(R.id.btn_download);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        txtStatus = (TextView) findViewById(R.id.text_status);
        updateButtons();

        btnDownload.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(new DownloadReceiver(), intentFilter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_download:
                Uri uriDownload = Uri.parse("https://cloudup.com/files/inYVmLryD4p/download");
                downloadFile(uriDownload);
                break;
            case R.id.btn_cancel:
                downloadManager.remove(downloadId);
        }
        updateButtons();
    }

    private void updateButtons() {
        if (btnCancel.isEnabled()) {
            btnCancel.setEnabled(false);
            btnDownload.setEnabled(true);
        } else {
            btnCancel.setEnabled(true);
            btnDownload.setEnabled(false);
        }
    }

    private void downloadFile(Uri uri) {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle("DownloadManagerDemo");
        request.setDescription("Downloading File, please wait....");

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());

        downloadId = downloadManager.enqueue(request);

        handlerUpdate = new Handler();
        handlerUpdate.post(runnableUpdate);
    }

    private void updateStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {

            int colStatusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int colReasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int colFileIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);

            int status = cursor.getInt(colStatusIndex);
            int reason = cursor.getInt(colReasonIndex);
            String fileName = cursor.getString(colFileIndex);

            String statusTxt = null, reasonStr = null;
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    statusTxt = "STATUS_FAILED";
                    done = true;
                    switch (reason) {
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            reasonStr = "ERROR_CANNOT_RESUME";
                            break;
                    }
                    break;
                case DownloadManager.STATUS_PENDING:
                    statusTxt = "STATUS_PENDING";
                    break;
                case DownloadManager.STATUS_RUNNING:
                    statusTxt = "STATUS_RUNNING";
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusTxt = "STATUS_SUCCESSFUL";
                    done = true;
                    break;

            }
            txtStatus.setText(statusTxt + " " + (reasonStr != null ? reasonStr : ""));
        }
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            long refId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (refId == MainActivity.this.downloadId) {
                Toast.makeText(MainActivity.this, "download completed", Toast.LENGTH_SHORT).show();
            }
            MainActivity.this.updateButtons();
        }
    }

    /*private DownloadManager downloadManager;
    private long refid;
    private Uri Download_Uri;
    ArrayList<Long> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        Download_Uri = Uri.parse("https://cloudup.com/files/inYVmLryD4p/download");

        TextView btnSingle = (TextView) findViewById(R.id.single);

        TextView btnMultiple = (TextView) findViewById(R.id.multiple);


        if (!isStoragePermissionGranted()) {

        }

        btnMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                list.clear();

                for (int i = 0; i < 2; i++) {
                    DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    request.setAllowedOverRoaming(false);
                    request.setTitle("GadgetSaint Downloading " + "Sample_" + i + ".png");
                    request.setDescription("Downloading " + "Sample_" + i + ".png");
                    request.setVisibleInDownloadsUi(true);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_Uri.getLastPathSegment());

                    refid = downloadManager.enqueue(request);

                    Log.e("OUTNM", "" + refid);

                    list.add(refid);

                }

            }
        });


        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                list.clear();

                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle("GadgetSaint Downloading " + "Sample" + ".png");
                request.setDescription("Downloading " + "Sample" + ".png");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/GadgetSaint/" + "/" + "Sample" + ".png");


                refid = downloadManager.enqueue(request);

                Log.e("OUT", "" + refid);

                list.add(refid);

            }
        });

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Log.e("IN", "" + referenceId);

            list.remove(referenceId);

            if (list.isEmpty()) {

                Log.e("INSIDE", "" + referenceId);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("GadgetSaint")
                                .setContentText("All Download completed");

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());

            }

        }
    };


    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(onComplete);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission granted

        }
    }*/
}
