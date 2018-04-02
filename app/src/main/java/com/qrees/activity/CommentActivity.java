package com.qrees.activity;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.qrees.R;
import com.qrees.adapter.CommentsAdapter;
import com.qrees.model.CommentsListModel;
import com.qrees.model.VideosListModel;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener{

    private Session session;
    private VideosListModel videosListModel;
    private ImageView iv_for_send;
    private EditText ed_for_comment;
    private ArrayList<CommentsListModel> fullCommentList;
    private CommentsAdapter commentsAdapter;
    private RecyclerView recycler_view;
    private TextView tv_for_noComment;
    //private int countCommentG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        initView();

        session = new Session(this);
        Bundle extras = getIntent().getExtras();
        videosListModel = (VideosListModel) extras.getSerializable("videoList");

        iv_for_send.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(false);

        fullCommentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(fullCommentList, this);
        getCommentsListAPI();
        recycler_view.setAdapter(commentsAdapter);
    }

    private void initView() {
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        ImageView iv_for_back = findViewById(R.id.iv_for_back);
        iv_for_send = findViewById(R.id.iv_for_send);
        tv_for_noComment = findViewById(R.id.tv_for_noComment);
        ed_for_comment = findViewById(R.id.ed_for_comment);
        recycler_view = findViewById(R.id.recycler_view);
        tv_for_tittle.setText(R.string.comment_title);
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

    public void addCommentAPI(final String commentText) {

        if (Utils.isNetworkAvailable(this)) {
            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this,pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constant.URL_WITH_LOGIN + "postComment", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equalsIgnoreCase("SUCCESS")) {
                            //Toast.makeText(CommentActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CommentActivity.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CommentActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("post_id", videosListModel.postId);
                    params.put("content", commentText);
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
            VolleySingleton.getInstance(CommentActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(CommentActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public void getCommentsListAPI() {

        if (Utils.isNetworkAvailable(this)) {

            final Dialog pDialog = new Dialog(this);
            Constant.myDialog(this,pDialog);
            pDialog.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.GET, Constant.URL_WITH_LOGIN + "getCommentList?post_id="+videosListModel.postId+"&limit&start", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("commentList");
                            if (jsonArray != null) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    CommentsListModel commentsListModel = new Gson().fromJson(object.toString(), CommentsListModel.class);
                                    fullCommentList.add(commentsListModel);
                                }
                                recycler_view.setAdapter(commentsAdapter);
                                //start = start + 0;
                            }
                        } else {
                            Toast.makeText(CommentActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        int size = fullCommentList.size();
                        if (size == 0) {
                            tv_for_noComment.setVisibility(View.VISIBLE);
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
                    Toast.makeText(CommentActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();

                    headers.put("authToken", session.getAuthToken());

                    return headers;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(CommentActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(CommentActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_for_send:
                String commentText = ed_for_comment.getText().toString().trim();

                if (commentText.equalsIgnoreCase("")) {
                    ed_for_comment.requestFocus();
                    return;
                } else {
                    addCommentAPI(commentText);
                    tv_for_noComment.setVisibility(View.GONE);
                    ed_for_comment.setText("");

                    CommentsListModel commentsListModel = new CommentsListModel();
                    commentsListModel.profileImage = session.getProfileImage();
                    commentsListModel.name = session.getFullName();
                    commentsListModel.day = "0 seconds ago";
                    commentsListModel.content = commentText;

                    /*int i = Integer.parseInt(videosListModel.comments)+1;
                    videosListModel.comments = String.valueOf(i);
                    countCommentG = i;*/
                    fullCommentList.add(commentsListModel);
                    commentsAdapter.notifyDataSetChanged();

                    recycler_view.smoothScrollToPosition(fullCommentList.size());

                    Utils.hideKeyboard(this);
                }

                break;
        }
    }
}
