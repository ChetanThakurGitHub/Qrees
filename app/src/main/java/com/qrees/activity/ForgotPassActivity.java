package com.qrees.activity;

import android.app.Dialog;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qrees.R;
import com.qrees.util.Constant;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_for_email;
    private RelativeLayout layout_for_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        initView();

        layout_for_button.setOnClickListener(this);
    }

    private void initView() {
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        ImageView iv_for_back = findViewById(R.id.iv_for_back);
        et_for_email = findViewById(R.id.et_for_email);
        layout_for_button = findViewById(R.id.layout_for_button);
        tv_for_tittle.setText(R.string.password_title);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.layout_for_button:
                validation();
                break;
        }
    }

    private void validation() {
        String email = et_for_email.getText().toString().trim();
        if (email.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.email_v, Toast.LENGTH_SHORT).show();
        }else if (Utils.Validationemail(email, this)) {
            forgotPasswordAPI(email);
        }
    }

    public void forgotPasswordAPI(final String email) {

        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this,pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITHOUT_LOGIN + "forgotPassword", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            Intent intent = new Intent(ForgotPassActivity.this,LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(ForgotPassActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPassActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ForgotPassActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("email", email);
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(ForgotPassActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(ForgotPassActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
