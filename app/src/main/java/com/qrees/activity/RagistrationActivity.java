package com.qrees.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.qrees.R;
import com.qrees.hepler.PermissionAll;
import com.qrees.model.UserFullDetail;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.StatusBarUtil;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.AppHelper;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.qrees.util.Constant.MY_PERMISSIONS_REQUEST_CAMERA;

public class RagistrationActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout layout_for_login;
    private ImageView iv_for_profile;
    private RelativeLayout layout_for_signup;
    private EditText et_for_fullName,et_for_email,et_for_password;
    private Dialog dialog;
    private Bitmap bitmap;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_ragistration);
        initView();

        session = new Session(this);
        layout_for_login.setOnClickListener(this);
        iv_for_profile.setOnClickListener(this);
        layout_for_signup.setOnClickListener(this);
    }

    private void initView(){
        layout_for_login = findViewById(R.id.layout_for_login);
        iv_for_profile = findViewById(R.id.iv_for_profile);
        et_for_fullName = findViewById(R.id.et_for_fullName);
        et_for_email = findViewById(R.id.et_for_email);
        et_for_password = findViewById(R.id.et_for_password);
        layout_for_signup = findViewById(R.id.layout_for_signup);
    }

    private void userImageClick() {
        dialog = new Dialog(RagistrationActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.take_picture);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.chackCameraPermission(RagistrationActivity.this);

        LinearLayout layout_for_camera = dialog.findViewById(R.id.layout_for_camera);
        LinearLayout layout_for_gallery = dialog.findViewById(R.id.layout_for_gallery);

        layout_for_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Constant.CAMERA);
                dialog.dismiss();
            }
        });
        layout_for_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.GALLERY);
                dialog.dismiss();
            }
        });
        dialog.show();
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
                    }
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(RagistrationActivity.this, "Deny Location Permission", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
            break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  Constant.GALLERY && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                if(bitmap!= null) {
                    iv_for_profile.setPadding(0,0,0,0);
                    iv_for_profile.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (requestCode == Constant.CAMERA && resultCode == RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if(bitmap!= null) {
                    iv_for_profile.setPadding(0,0,0,0);
                    iv_for_profile.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void validation(){
        String fullName = et_for_fullName.getText().toString().trim();
        String email = et_for_email.getText().toString().trim();
        String password = et_for_password.getText().toString().trim();
        if (fullName.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.fullname_v, Toast.LENGTH_SHORT).show();
        }else if (fullName.length() < 3) {
            Toast.makeText(this, R.string.fullname_required, Toast.LENGTH_LONG).show();
            et_for_fullName.requestFocus();
        }else if (email.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.email_v, Toast.LENGTH_SHORT).show();
        }else if (!Utils.Validationemail(et_for_email.getText().toString(), this)) {
        } else if (password.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.password_v, Toast.LENGTH_SHORT).show();
        }else if (password.length() < 6) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_LONG).show();
            et_for_password.requestFocus();
        }else {
            doRegistration(fullName,email,password);
        }
    }

    private void doRegistration(final String fullName, final String email, final String password) {

        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this,pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITHOUT_LOGIN + "userRegistration", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {

                            String userDetail = jsonObject.getString("userDetail");

                            UserFullDetail  userFullDetail = new Gson().fromJson(userDetail.toString(),UserFullDetail.class);
                            userFullDetail.password = password;
                            session.createSession(userFullDetail);

                         /*   session.setEmailR("");
                            session.setPasswordR("");
                            String s = session.getEmailR();*/

                            session.logoutMyPre();

                            Intent intent = new Intent(RagistrationActivity.this,HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(RagistrationActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(RagistrationActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("name", fullName);
                    params.put("email", email);
                    params.put("password", password);
                    params.put("contactNumber", "");
                    params.put("countryCode", "");
                    params.put("location", "");
                    params.put("latitude", "");
                    params.put("longitude", "");
                    params.put("deviceType", "");
                    params.put("deviceToken", "");
                    params.put("socialId", "");
                    params.put("socialType", "2");
                    params.put("firebase_id", "");

                    if (bitmap == null){
                        params.put("profilePic", "");
                    }
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<String, DataPart>();
                    if (bitmap != null) {
                        params.put("profileImage", new VolleyMultipartRequest.DataPart("profilePic.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                    }
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(RagistrationActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(RagistrationActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.layout_for_login:
                onBackPressed();
                break;
            case R.id.iv_for_profile:
                userImageClick();
                break;
            case R.id.layout_for_signup:
                validation();
                break;
        }
    }

}
