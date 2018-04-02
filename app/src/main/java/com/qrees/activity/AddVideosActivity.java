package com.qrees.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnSuccessListener;
import com.qrees.R;
import com.qrees.hepler.PermissionAll;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.ImageVideoUtil;
import com.qrees.util.NotificationUtil;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.qrees.util.Constant.MY_PERMISSIONS_REQUEST_CAMERA;
import static com.qrees.util.Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
import static com.qrees.util.Constant.RECORD_AUDIO;
import static com.qrees.util.Constant.URL_WITH_LOGIN;

public class AddVideosActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_for_caption;
    private LinearLayout layout_for_facebook, layout_for_twitter, layout_for_linkedin;
    private boolean checkBox_facebook = false, checkBox_twitter = false, checkBox_linkedin = false;
    private ImageView iv_for_facebook, iv_for_twitter, iv_for_linkedin, iv_for_video,iv_for_thum;
    private TextView tv_for_laction, tv_for_videoP;
    private double latitude, longitude;
    private RelativeLayout layout_for_post,layout_for_videoAdd;
    private String location, caption;
    private FusedLocationProviderClient mFusedLocationClient;
    private Session session;
    private Uri videoUri;
    private ProgressBar progressbar;
    private static int ID = 100;
    private NotificationManager mNotifyManager;
    private Notification.Builder mBuilder;
    private Dialog pDialog;
    private Bitmap bitmap2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_videos);
        initView();

        session = new Session(this);
        PermissionAll permissionAll = new PermissionAll();
        permissionAll.checkLocationPermission(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        Utils.checkGpsStatus(this);

        permissionAll = new PermissionAll();
        permissionAll.chackCameraPermission(AddVideosActivity.this);

        layout_for_facebook.setOnClickListener(this);
        layout_for_twitter.setOnClickListener(this);
        layout_for_linkedin.setOnClickListener(this);
        tv_for_laction.setOnClickListener(this);
        iv_for_video.setOnClickListener(this);
        layout_for_post.setOnClickListener(this);
    }

    private void initView() {
        ImageView iv_for_back = findViewById(R.id.iv_for_back);
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        iv_for_back.setVisibility(View.VISIBLE);
        iv_for_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        tv_for_tittle.setText(R.string.add_video);

        et_for_caption = findViewById(R.id.et_for_caption);
        layout_for_facebook = findViewById(R.id.layout_for_facebook);
        layout_for_twitter = findViewById(R.id.layout_for_twitter);
        layout_for_linkedin = findViewById(R.id.layout_for_linkedin);
        iv_for_facebook = findViewById(R.id.iv_for_facebook);
        iv_for_twitter = findViewById(R.id.iv_for_twitter);
        iv_for_linkedin = findViewById(R.id.iv_for_linkedin);
        tv_for_laction = findViewById(R.id.tv_for_laction);
        iv_for_video = findViewById(R.id.iv_for_video);
        layout_for_videoAdd = findViewById(R.id.layout_for_videoAdd);
        layout_for_post = findViewById(R.id.layout_for_post);
        iv_for_thum = findViewById(R.id.iv_for_thum);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Utils.hideKeyboard(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_for_facebook:
                if (!checkBox_facebook) {
                    iv_for_facebook.setBackgroundResource(R.drawable.check);
                    checkBox_facebook = true;
                } else {
                    iv_for_facebook.setBackgroundResource(R.drawable.uncheck);
                    checkBox_facebook = false;
                }
                break;
            case R.id.layout_for_twitter:
                if (!checkBox_twitter) {
                    iv_for_twitter.setBackgroundResource(R.drawable.check);
                    checkBox_twitter = true;
                } else {
                    iv_for_twitter.setBackgroundResource(R.drawable.uncheck);
                    checkBox_twitter = false;
                }
                break;
            case R.id.layout_for_linkedin:
                if (!checkBox_linkedin) {
                    iv_for_linkedin.setBackgroundResource(R.drawable.check);
                    checkBox_linkedin = true;
                } else {
                    iv_for_linkedin.setBackgroundResource(R.drawable.uncheck);
                    checkBox_linkedin = false;
                }
                break;
            case R.id.tv_for_laction:
                PermissionAll permissionAll = new PermissionAll();
                permissionAll.checkLocationPermission(this);
                addressClick();
                tv_for_laction.setEnabled(false);
                break;
            case R.id.iv_for_video:
                takeVideoClick();
                break;
            case R.id.layout_for_post:
                if (latitude == 0.0 && longitude == 0.0) {
                    locaitonDialog();
                } else {
                    validation();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideKeyboard(this);
    }

    private void takeVideoClick() {
        final Dialog dialog = new Dialog(AddVideosActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.take_picture);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.chackCameraPermission(AddVideosActivity.this);

        LinearLayout layout_for_camera = dialog.findViewById(R.id.layout_for_camera);
        LinearLayout layout_for_gallery = dialog.findViewById(R.id.layout_for_gallery);
        TextView tv_for_camera = dialog.findViewById(R.id.tv_for_camera);
        tv_for_camera.setText(R.string.video_take);

        layout_for_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
                dialog.dismiss();
            }
        });
        layout_for_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVideoFromGallery();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void dispatchTakeVideoIntent() {

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.checkAudioPermission(this);

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, Constant.REQUEST_VIDEO_CAPTURE);
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        3);
            }
        } else {
            // permission has been granted, continue as usual
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                latitude = Double.valueOf(String.valueOf(location.getLatitude()));
                                longitude = Double.valueOf(String.valueOf(location.getLongitude()));

                                try {
                                    latlong(latitude, longitude);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    } // parmission for location and code after parmission

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            tv_for_laction.setEnabled(true);
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;

                location = String.valueOf(place.getName());
                tv_for_laction.setText(location);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == Constant.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                videoUri = data.getData();
                layout_for_videoAdd.setVisibility(View.VISIBLE);
                iv_for_video.setVisibility(View.GONE);
            }
        }
        if (requestCode == Constant.SELECT_VIDEO_REQUEST && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                videoUri = data.getData();
                layout_for_videoAdd.setVisibility(View.VISIBLE);
                iv_for_video.setVisibility(View.GONE);
            } else {
                Toast.makeText(getApplicationContext(), "Failed to select video", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void selectVideoFromGallery() {
        Intent intent;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, Constant.SELECT_VIDEO_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        //success permission granted & call Location method
                        dispatchTakeVideoIntent();
                    }
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(AddVideosActivity.this, "Deny Camera Permission", Toast.LENGTH_SHORT).show();

                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        //success permission granted & call Location method
                        dispatchTakeVideoIntent();
                    }
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(AddVideosActivity.this, "Deny Storage Permission", Toast.LENGTH_SHORT).show();

                }
            }
            break;

            case RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                        //success permission granted & call Location method
                        dispatchTakeVideoIntent();
                    }
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(AddVideosActivity.this, "Deny Audio Permission", Toast.LENGTH_SHORT).show();

                }
            }
            break;
        }
    }

    private void latlong(Double latitude, Double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

        location = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        tv_for_laction.setText(location);

    } // latlog to address find

    private void addressClick() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(AddVideosActivity.this);
            startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    } // method for address button click

    private void validation() {
        caption = et_for_caption.getText().toString().trim();
        if (videoUri == null) {
            Toast.makeText(this, R.string.video_v, Toast.LENGTH_SHORT).show();
        } else if (caption.equalsIgnoreCase("")) {
            Toast.makeText(this, R.string.caption_v, Toast.LENGTH_SHORT).show();
        } else {
            /*Intent i = new Intent(this, ServiceUploadFile.class);
            i.putExtra("videoUri", videoUri);
            i.putExtra("caption", caption);
            i.putExtra("location", location);
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            this.startService(i);
            Toast.makeText(this, "Uploading....", Toast.LENGTH_SHORT).show();

            et_for_caption.setText("");
            videoUri = null;
            layout_for_videoAdd.setVisibility(View.GONE);
            iv_for_video.setVisibility(View.VISIBLE);*/

            uploadeVideo();
        }
    }

    private void locaitonDialog() {
        final Dialog dialog = new Dialog(AddVideosActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.location_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.chackCameraPermission(AddVideosActivity.this);

        Button btn_for_cancel = dialog.findViewById(R.id.btn_for_cancel);
        Button btn_for_retry = dialog.findViewById(R.id.btn_for_retry);

        btn_for_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_for_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(AddVideosActivity.this);
                checkLocationPermission();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void uploadeVideo() {

        videoDialog();

//thumb image code
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(videoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        bitmap2 = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MICRO_KIND);
        iv_for_thum.setImageBitmap(bitmap2);


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                mNotifyManager = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new Notification.Builder(getApplicationContext());
                mBuilder.setSmallIcon(R.drawable.logo);

                String path = ImageVideoUtil.generatePath(videoUri, AddVideosActivity.this);
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
                return null;
            }
        }.execute();
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
        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
                mBuilder.setProgress(Constant.PROGRESS_BAR_MAX, (int) Math.round(progress * Constant.PROGRESS_BAR_MAX), false);
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
        Future<Void> mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
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
        AddVideosActivity.this.grantUriPermission("com.qrees.service", Uri.fromFile(file), Intent.FLAG_GRANT_READ_URI_PERMISSION);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        //client
        //  OkHttpClient okHttpClient = new OkHttpClient();
//request builder
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
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
                mBuilder.setContentTitle("Upload Video...").setContentText("Upload in progress");
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
                progressbar.setProgress((int) (percent * 100));
                tv_for_videoP.setText(" " + (int) (percent * 100) + "% ......");
            }

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressFinish() {
                super.onUIProgressFinish();
                Log.e("TAG", "onUIProgressFinish:");
                NotificationUtil.cancelNotification(getApplicationContext(), ID);
                Intent intentNew = new Intent("FILTER"); //FILTER is a string to identify this intent
                sendBroadcast(intentNew);
                Constant.isVideoUploading = false;
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
                Constant.isVideoUploading = false;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Video is corrupted",
                                Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                        bitmap2 = null;
                    }
                });
                NotificationUtil.cancelNotification(getApplicationContext(), ID);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Constant.isVideoUploading = false;
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

                pDialog.dismiss();
                bitmap2 = null;
                AddVideosActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //onBackPressed();
                        et_for_caption.setText("");
                        videoUri = null;
                        layout_for_videoAdd.setVisibility(View.GONE);
                        iv_for_video.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

    private void videoDialog() {

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.checkWriteStoragePermission(this);

        pDialog = new Dialog(AddVideosActivity.this);
        pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pDialog.setCancelable(false);
        pDialog.setContentView(R.layout.progress_bar_video);
        progressbar = pDialog.findViewById(R.id.progressbar);
        tv_for_videoP = pDialog.findViewById(R.id.tv_for_videoP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progressbar.setMin(1);
        }
        progressbar.setMax(100);

        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pDialog.show();
    }

    private void uploadVideoAPI() {

        if (Utils.isNetworkAvailable(this)) {
            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this, pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    Constant.URL_WITH_LOGIN + "addVideo", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);

                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String userstatus = jsonObject.getString("userstatus");
                        String message = jsonObject.getString("message");

                        if (status.equals("SUCCESS")) {
                            if (userstatus.equals("1")) {


                            } else {
                                Utils.customAlertDialog(AddVideosActivity.this, "Alert!", "You are temporary inactive by admin");
                            }
                        } else {
                            Toast.makeText(AddVideosActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                    pDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Constant.errorHandle(error, AddVideosActivity.this);
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(AddVideosActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("location", location);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));
                    params.put("video", String.valueOf(videoUri));

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();

                    headers.put("authToken", session.getAuthToken());

                    return headers;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<String, DataPart>();
                    if (videoUri != null) {
                        // params.put("video", new VolleyMultipartRequest.DataPart("profilePic.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                    }
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(AddVideosActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(AddVideosActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

}
