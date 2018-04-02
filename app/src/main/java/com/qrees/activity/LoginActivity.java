package com.qrees.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.qrees.R;
import com.qrees.model.UserFullDetail;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.StatusBarUtil;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean checkBox = false;
    private LinearLayout layout_for_remember,layout_for_signup;
    private ImageView iv_uncheck;
    private EditText et_for_email,et_for_password;
    private RelativeLayout layout_for_login;
    private Session session;
    private TextView tv_for_forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_login);
        initView();

        session = new Session(this);

        if (!session.getEmailR().equals("")&&session.getEmailR() != null) {
            checkBox = true;
            et_for_email.setText(session.getEmailR());
            et_for_password.setText(session.getPasswordR());
            iv_uncheck.setBackgroundResource(R.drawable.check);
        }

        layout_for_remember.setOnClickListener(this);
        layout_for_signup.setOnClickListener(this);
        layout_for_login.setOnClickListener(this);
        tv_for_forgotPassword.setOnClickListener(this);

    }

    private void initView(){
        layout_for_remember = findViewById(R.id.layout_for_remember);
        layout_for_signup = findViewById(R.id.layout_for_signup);
        iv_uncheck= findViewById(R.id.iv_uncheck);
        et_for_email= findViewById(R.id.et_for_email);
        et_for_password= findViewById(R.id.et_for_password);
        layout_for_login= findViewById(R.id.layout_for_login);
        tv_for_forgotPassword= findViewById(R.id.tv_for_forgotPassword);
    }

    private void validation(){
        String email = et_for_email.getText().toString().trim();
        String password = et_for_password.getText().toString().trim();
        if (email.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.email_v, Toast.LENGTH_SHORT).show();
        }else if (!Utils.Validationemail(et_for_email.getText().toString(), this)) {
        }else if (password.equalsIgnoreCase("")){
            Toast.makeText(this, R.string.password_v, Toast.LENGTH_SHORT).show();
        }else if (password.length() < 6) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_LONG).show();
            et_for_password.requestFocus();
        }else{
            login(email,password);
        }
    }

    public void login(final String email, final String password) {

        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this,pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITHOUT_LOGIN + "userLogin", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            JSONObject userDetail = jsonObject.getJSONObject("userDetail");

                            UserFullDetail userFullDetail = new Gson().fromJson(userDetail.toString(),UserFullDetail.class);
                            userFullDetail.password = password;
                            if (userFullDetail.status.equals("1")) {
                                session.createSession(userFullDetail);
                                if (!checkBox){
                                    session.logoutMyPre();
                                }
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Your account has been inactivated by admin, please contact to activate", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("email", email);
                    params.put("password", password);

                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(LoginActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.layout_for_remember:
                if (!checkBox) {
                    iv_uncheck.setBackgroundResource(R.drawable.check);
                    checkBox = true;
                } else {
                    iv_uncheck.setBackgroundResource(R.drawable.uncheck);
                    checkBox = false;
                }
                break;
            case R.id.layout_for_signup:
                Intent intent = new Intent(LoginActivity.this,RagistrationActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_for_login:
                validation();
                break;
            case R.id.tv_for_forgotPassword:
                Intent intent1 = new Intent(LoginActivity.this,ForgotPassActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up);
                break;
        }
    }
}
