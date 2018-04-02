package com.qrees.service;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qrees.R;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.ImageVideoUtil;
import com.qrees.util.NotificationUtil;
import com.qrees.util.Utils;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.qrees.util.Constant.URL_WITH_LOGIN;
import static io.github.lizhangqu.coreprogress.ProgressHelper.*;

/**
 * Created by android-5 on 15/11/17.
 */

public class ServiceUploadFile extends Service {
    private static final int PROGRESS_BAR_MAX = 1000;
    private static int ID = 100;
    // private MediaType MEDIA_TYPE = MediaType.parse("application/json");
    private NotificationManager mNotifyManager;
    private Notification.Builder mBuilder;
    private HashMap<String, String> hashMap;
    private String FILE_PROVIDER_AUTHORITY = "com.qrees.fileprovider";
    private Future<Void> mFuture;
    private String caption, location;
    private Double latitude, longitude;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // super.onStartCommand(intent, flags, startId);
        Log.e("TAG", "onStartCommand: Call");

        if (intent != null) {
            String mSelectdVideo = intent.getExtras().get("videoUri") + "";
            //String mSelectdVideo = "chetan";
            caption = (String) intent.getExtras().get("caption");
            location = (String) intent.getExtras().get("location");
            latitude = (Double) intent.getExtras().get("latitude");
            longitude = (Double) intent.getExtras().get("longitude");

            //hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");
            //  ID = (int)intent.getExtras().get("ID");
            //  if (Constant.uploadModels.size() > 0)
            doBackGroundTask(mSelectdVideo);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doBackGroundTask(String mSelectdVideo) {
        mNotifyManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.logo);

        Uri videoUri = Uri.parse(mSelectdVideo);

        String path = ImageVideoUtil.generatePath(videoUri, this);
        File file = new File(path);

        // Get length of file in bytes
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB > 10) {
            compressVideo(videoUri, file);
        } else {
            apiCallForUploadVideo(file);
        }
    }

    private void compressVideo(Uri uri, final File tmpFile) {

        final File file;
        try {
            File outputDir = new File(getExternalFilesDir(null), "outputs");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            file = File.createTempFile("transcode_test", ".mp4", outputDir);
        } catch (IOException e) {
            Log.e("TAG", "Failed to create temporary file.", e);
            Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return;
        }
        ContentResolver resolver = getContentResolver();
        final ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            Log.d("Could not open '" + uri.toString() + "'", e.getMessage());
            // Toast.makeText(getApplicationContext(), "File not found.", Toast.LENGTH_LONG).show();
            return;
        }
        final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        //final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        // progressBar.setVisibility(View.VISIBLE);
        //  progressBar.setMax(PROGRESS_BAR_MAX);
        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
                mBuilder.setProgress(PROGRESS_BAR_MAX, (int) Math.round(progress * PROGRESS_BAR_MAX), false);
                // mBuilder.setContentText("" + (int) Math.round(progress * PROGRESS_BAR_MAX/10) + "%");
                mBuilder.setContentTitle("Uploading Video...");
                mNotifyManager.notify(ID, mBuilder.build());
            }

            @Override
            public void onTranscodeCompleted() {
                Log.d("TAG", "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);
                apiCallForUploadVideo(file);
            }

            @Override
            public void onTranscodeCanceled() {
                onTranscodeFinished(false, "Compress canceled.", parcelFileDescriptor);
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                // Get length of file in bytes
                long fileSizeInBytes = tmpFile.length();
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                long fileSizeInKB = fileSizeInBytes / 1024;
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                long fileSizeInMB = fileSizeInKB / 1024;

                if (fileSizeInMB <= 30) {
                    apiCallForUploadVideo(tmpFile);
                }
            }
        };
        Log.d("TAG", "transcoding into " + file);
        mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
                // MediaFormatStrategyPresets.createAndroid720pStrategy(8000 * 1000, 128 * 1000, 1), listener);
                MediaFormatStrategyPresets.createExportPreset960x540Strategy(), listener);
        // switchButtonEnabled(true);
    }

    private void onTranscodeFinished(boolean isSuccess, String toastMessage, ParcelFileDescriptor parcelFileDescriptor) {
        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            Log.w("Error while closing", e);
        }
    }

    private void apiCallForUploadVideo(File file) {

        Session session = new Session(this);
        ServiceUploadFile.this.grantUriPermission("com.qrees.service", Uri.fromFile(file), Intent.FLAG_GRANT_READ_URI_PERMISSION);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        //client
        //  OkHttpClient okHttpClient = new OkHttpClient();
//request builder
        Request.Builder builder = new Request.Builder();
        builder.url(URL_WITH_LOGIN + "addPost");
        builder.addHeader("Content-Type", "application/json");
        builder.addHeader("cache-control", "no-cache");
        builder.addHeader("authToken", session.getAuthToken());
//your original request body
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        bodyBuilder.addFormDataPart("video", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
        bodyBuilder.addFormDataPart("caption", caption);
        bodyBuilder.addFormDataPart("location", location);
        bodyBuilder.addFormDataPart("latitude", String.valueOf(latitude));
        bodyBuilder.addFormDataPart("longitude", String.valueOf(longitude));
        MultipartBody body = bodyBuilder.build();

//wrap your original request body with progress
        RequestBody requestBody = ProgressHelper.withProgress(body, new ProgressUIListener() {

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressStart(long totalBytes) {
                super.onUIProgressStart(totalBytes);
                Log.e("TAG", "onUIProgressStart:" + totalBytes);
                mBuilder.setContentTitle("UpLoad Video...").setContentText("UpLoad in progress");
                mNotifyManager.notify(ID, mBuilder.build());
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                Log.e("TAG", "=============start===============");
                Log.e("TAG", "numBytes:" + numBytes);
                Log.e("TAG", "totalBytes:" + totalBytes);
                Log.e("TAG", "percent:" + percent * 100);
                Log.e("TAG", "speed:" + (speed * 1000 / 1024 / 1024) + "  MB/sec");
                Log.e("TAG", "============= end ===============");
                String sp = new DecimalFormat("##.##").format(speed * 1000 / 1024 / 1024);
                mBuilder.setContentTitle("Upload Video...");
                mBuilder.setProgress(100, (int) (percent * 100), false);
                mBuilder.setContentText(sp + "  MB/sec");
                mNotifyManager.notify(ID, mBuilder.build());
            }

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressFinish() {
                super.onUIProgressFinish();
                Log.e("TAG", "onUIProgressFinish:");
                NotificationUtil.cancelNotification(getApplicationContext(), ID);
                Intent intentNew = new Intent("FILTER"); //FILTER is a string to identify this intent
                sendBroadcast(intentNew);
                Constant.isVideoUploading=false;
                //  if (Constant.uploadModels.size() == pos + 1) Constant.uploadModels.clear();
            }
        });
        //post the wrapped request body
        builder.post(requestBody);
//call
        Call call = okHttpClient.newCall(builder.build());
//enqueue
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "=============onFailure===============");
                Constant.isVideoUploading=false;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Check your Internet Connection.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                //Toast.makeText(ServiceUploadFile.this, "Check your Internet Connection.", Toast.LENGTH_SHORT).show();
                //mNotifyManager.cancel(ID);
                NotificationUtil.cancelNotification(getApplicationContext(), ID);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Constant.isVideoUploading=false;
                if (!response.isSuccessful()) try {
                    throw new IOException("Unexpected code " + response);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String jsonResponse = response.body().string().toString();
                Log.e("TAG", "=============onResponse===============");
                Log.e("TAG", "request headers:" + response.request().headers());
                Log.e("TAG", "response headers:" + response.headers());
                Log.e("TAG", "response Body:" + jsonResponse);
                /*JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonResponse);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");
                    Toast.makeText(ServiceUploadFile.this, message, Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("TAG", "onTaskRemoved: Call");
        Log.e("TAG", "notification Id: " + ID);
        NotificationUtil.cancelNotification(getApplicationContext(), ID);
        super.onTaskRemoved(rootIntent);
    }

}
