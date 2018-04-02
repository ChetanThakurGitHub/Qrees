package com.qrees.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qrees.R;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePassActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_for_newPassword, et_for_CPassword;
    private RelativeLayout layout_for_button;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        initView();
        session = new Session(this);
        layout_for_button.setOnClickListener(this);
    }

    private void initView() {
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        ImageView iv_for_back = findViewById(R.id.iv_for_back);
        et_for_newPassword = findViewById(R.id.et_for_newPassword);
        et_for_CPassword = findViewById(R.id.et_for_CPassword);
        layout_for_button = findViewById(R.id.layout_for_button);
        tv_for_tittle.setText(R.string.change_pass_title);
        iv_for_back.setVisibility(View.VISIBLE);
        iv_for_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void validation() {
        String newPassword = et_for_newPassword.getText().toString().trim();
        String confirmPassword = et_for_CPassword.getText().toString().trim();

        if (newPassword.equalsIgnoreCase("")) {
            Toast.makeText(this, R.string.password_v, Toast.LENGTH_SHORT).show();
        } else if (newPassword.length() < 6) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_LONG).show();
            et_for_newPassword.requestFocus();
        } else if (confirmPassword.equalsIgnoreCase("")) {
            Toast.makeText(this, R.string.password_v, Toast.LENGTH_SHORT).show();
        } else if (confirmPassword.length() < 6) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_LONG).show();
            et_for_CPassword.requestFocus();
        } else {
            if (newPassword.equals(confirmPassword)) {
                customAlertDialog(newPassword);
            } else {
                Toast.makeText(this, "Password not match", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void customAlertDialog(final String newPassword) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Alert");
        dialog.setMessage("your session will expire, when you change your password");
        dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                changePasswordAPI(newPassword);
            }
        });
        dialog.setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Action for "Cancel".
            }
        });
        final AlertDialog alert = dialog.create();
        alert.show();
    }

    private void changePasswordAPI(final String newPassword) {

        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this, pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITH_LOGIN + "changePassword", new Response.Listener<NetworkResponse>() {
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
                            session.logout(ChangePassActivity.this);
                            session.logoutMyPre();
                            Intent i = new Intent(ChangePassActivity.this, HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            Toast.makeText(ChangePassActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ChangePassActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("oldPassword", session.getPassword());
                    params.put("newPassword", newPassword);
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("authToken", session.getAuthToken());
                    return headers;
                }
            };
            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(ChangePassActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(ChangePassActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_for_button:
                validation();
                break;
        }
    }
}
