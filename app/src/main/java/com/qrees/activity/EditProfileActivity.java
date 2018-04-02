package com.qrees.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qrees.R;
import com.qrees.hepler.PermissionAll;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.StatusBarUtil;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.AppHelper;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_for_back, profile_image;
    private EditText et_for_fullName, et_for_email;
    private RelativeLayout layout_for_changePassword, layout_for_update, layout_for_userImg;
    private Session session;
    private Dialog dialog;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_edit_profile);
        initView();

        session = new Session(this);
        String image = session.getProfileImage();
        if (image != null && !image.equals("")) {
            Picasso.with(this).load(image).into(profile_image);
        }
        et_for_fullName.setText(session.getFullName());
        et_for_email.setText(session.getEmail());
        iv_for_back.setOnClickListener(this);
        layout_for_userImg.setOnClickListener(this);
        layout_for_changePassword.setOnClickListener(this);
        layout_for_update.setOnClickListener(this);

    }

    private void initView() {
        iv_for_back = findViewById(R.id.iv_for_back);
        profile_image = findViewById(R.id.profile_image);
        et_for_fullName = findViewById(R.id.et_for_fullName);
        et_for_email = findViewById(R.id.et_for_email);
        layout_for_changePassword = findViewById(R.id.layout_for_changePassword);
        layout_for_update = findViewById(R.id.layout_for_update);
        layout_for_userImg = findViewById(R.id.layout_for_userImg);
    }

    private void userImageClick() {
        dialog = new Dialog(EditProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.take_picture);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.chackCameraPermission(EditProfileActivity.this);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.GALLERY && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                if (bitmap != null) {
                    profile_image.setPadding(0, 0, 0, 0);
                    profile_image.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (requestCode == Constant.CAMERA && resultCode == RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    profile_image.setPadding(0, 0, 0, 0);
                    profile_image.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void validation() {
        String fullName = et_for_fullName.getText().toString().trim();
        String email = et_for_email.getText().toString().trim();
        if (fullName.equalsIgnoreCase("")) {
            Toast.makeText(this, R.string.fullname_v, Toast.LENGTH_SHORT).show();
        } else if (fullName.length() < 3) {
            Toast.makeText(this, R.string.fullname_required, Toast.LENGTH_LONG).show();
            et_for_fullName.requestFocus();
        } else if (email.equalsIgnoreCase("")) {
            Toast.makeText(this, R.string.email_v, Toast.LENGTH_SHORT).show();
        } else if (!Utils.Validationemail(et_for_email.getText().toString(), this)) {
        } else {
            if (bitmap != null | !fullName.equals(session.getFullName()) | !email.equals(session.getEmail())) {
                updateProfileAPI(fullName, email);
            }
        }
    }

    private void updateProfileAPI(final String fullName, final String email) {
        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this, pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITH_LOGIN + "profileUpdate", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            pDialog.dismiss();
                            String userDetail = jsonObject.getString("userDetail");
                            JSONObject object = new JSONObject(userDetail);
                            String profileImage = object.getString("profileImage");
                            String name = object.getString("name");
                            String email = object.getString("email");

                            session.setEmailR(email);
                            session.setEmail(email);
                            session.setFullName(name);
                            session.setProfileImage(profileImage);

                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EditProfileActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("name", fullName);
                    params.put("email", email);

                    if (bitmap == null) {
                        params.put("profilePic", "");
                    }
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
                    if (bitmap != null) {
                        params.put("profileImage", new VolleyMultipartRequest.DataPart("profilePic.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                    }
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(EditProfileActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(EditProfileActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_for_back:
                finish();
                break;
            case R.id.layout_for_userImg:
                userImageClick();
                break;
            case R.id.layout_for_changePassword:
                Intent intent = new Intent(EditProfileActivity.this, ChangePassActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up);
                break;
            case R.id.layout_for_update:
                validation();
                break;
        }
    }
}
