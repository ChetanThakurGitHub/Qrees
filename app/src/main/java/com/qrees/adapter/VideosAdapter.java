package com.qrees.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qrees.R;
import com.qrees.activity.CommentActivity;
import com.qrees.activity.LoginActivity;
import com.qrees.model.VideosListModel;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by abc on 30/01/2018.
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {

    private List<VideosListModel> videosList;
    private Activity mContext;
    private Session session;

    public VideosAdapter(List<VideosListModel> dataList, Activity mContext) {
        this.videosList = dataList;
        this.mContext = mContext;
        session = new Session(mContext);
    }

    @Override
    public VideosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list, parent, false);
        return new VideosAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final VideosAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final VideosListModel videosListModel = videosList.get(position);

        if (videosListModel.profileImage != null && !videosListModel.profileImage.equals("")) {
            Picasso.with(mContext).load(videosListModel.profileImage).fit().into(holder.profile_image);}

        if (Integer.parseInt(videosListModel.views) < 1){
            holder.txt_view.setText("Views");}
        if (Integer.parseInt(videosListModel.likes) < 1){
            holder.txt_like.setText("Likes");}
        if (Integer.parseInt(videosListModel.comments) < 1){
            holder.txt_comment.setText("Comments");}

        holder.tv_for_name.setText(videosListModel.name);
        if (!videosListModel.day.equals("")) {
            holder.tv_for_daystatus.setText(videosListModel.day);}
        holder.tv_for_caption.setText("# "+videosListModel.caption);
        holder.tv_for_like.setText(videosListModel.likes);
        holder.tv_for_comment.setText(videosListModel.comments);
        holder.tv_for_location.setText(videosListModel.location);
        holder.tv_for_view.setText(videosListModel.views);

        if (videosListModel.videoThumbImage != null && !videosListModel.videoThumbImage.equals("")) {
            Picasso.with(mContext).load(videosListModel.videoThumbImage).fit().placeholder(R.color.darkTransparent).into(holder.iv_for_image);
        }else {
            Picasso.with(mContext).load(R.color.darkTransparent).fit().into(holder.iv_for_image);}

        holder.layout_for_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!session.getIsLogedIn()) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, CommentActivity.class);
                    intent.putExtra("videoList", videosListModel); //model
                    //intent.putExtra("position", position); //position
                    mContext.startActivityForResult(intent, 5);
                }
            }
        });

        holder.iv_for_playVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (session.getIsLogedIn()) {
                    String post_id = videosListModel.postId;
                    String API = "postView";
                    likeAndViewAPI(post_id,API);
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse(videosListModel.video);
                i.setDataAndType(data, "video/*");
                i.putExtra (MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
                try {
                    mContext.startActivity(i);
                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, R.string.video_play, Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.layout_for_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (videosListModel.isLike.equals("0")){
                    holder.iv_for_like.setImageResource(R.drawable.active_like);
                    holder.tv_for_postlike.setText("Unlike");

                    videosListModel.likes = ( videosListModel.likes + 1);
                    videosListModel.isLike = "1";
                    islike = "1";
                    holder.tv_for_like.setText("" + ( videosListModel.likes));

                }else if((videosListModel.isLike.equals("1"))){

                    holder.iv_for_like.setImageResource(R.drawable.like);
                    holder.tv_for_postlike.setText("Like");

                    videosListModel.likes = (videosListModel.likes - 1);
                    videosListModel.isLike = "0";
                    islike = "0";
                    holder.tv_for_like.setText("" + (videosListModel.likes));
                }*/
                if (session.getIsLogedIn()) {
                    String post_id = videosListModel.postId;
                    String API = "postLike";
                    likeAndViewAPI(post_id,API);

                    if (videosListModel.likeB) {
                        int like = Integer.parseInt(videosListModel.likes) + 1;
                        videosListModel.likes = String.valueOf(like);
                        videosListModel.likeB = false;
                    }else {
                        int like = Integer.parseInt(videosListModel.likes) - 1;
                        videosListModel.likes = String.valueOf(like);
                        videosListModel.likeB = true;
                    }

                    holder.tv_for_like.setText(videosListModel.likes);

                }else {
                    Intent intent = new Intent(mContext,LoginActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });

        holder.layout_for_shar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, videosListModel.video);
                sendIntent.setType("text/plain");
                mContext.startActivity(sendIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView profile_image, iv_for_image,iv_for_playVideo;
        private TextView tv_for_name, tv_for_daystatus, tv_for_caption, tv_for_like, tv_for_comment,
                tv_for_location,tv_for_view,txt_like,txt_comment,txt_view,tv_for_more;
        private LinearLayout layout_for_like, layout_for_comment, layout_for_shar;

        public ViewHolder(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            iv_for_image = itemView.findViewById(R.id.iv_for_image);
            iv_for_playVideo = itemView.findViewById(R.id.iv_for_playVideo);

            tv_for_name = itemView.findViewById(R.id.tv_for_name);
            tv_for_daystatus = itemView.findViewById(R.id.tv_for_daystatus);
            tv_for_caption = itemView.findViewById(R.id.tv_for_caption);
            tv_for_like = itemView.findViewById(R.id.tv_for_like);
            tv_for_comment = itemView.findViewById(R.id.tv_for_comment);
            tv_for_location = itemView.findViewById(R.id.tv_for_location);
            tv_for_view = itemView.findViewById(R.id.tv_for_view);

            layout_for_like = itemView.findViewById(R.id.layout_for_like);
            layout_for_comment = itemView.findViewById(R.id.layout_for_comment);
            layout_for_shar = itemView.findViewById(R.id.layout_for_shar);

            txt_like = itemView.findViewById(R.id.txt_like);
            txt_comment = itemView.findViewById(R.id.txt_comment);
            txt_view = itemView.findViewById(R.id.txt_view);

        }
    }

    private void likeAndViewAPI(final String post_id, String API) {

        if (Utils.isNetworkAvailable(mContext)) {
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    Constant.URL_WITH_LOGIN + API, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        //String responsed = jsonObject.getString("response");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Constant.errorHandle(error, mContext);
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(mContext, networkResponse + "", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("post_id", post_id);
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
            VolleySingleton.getInstance(mContext).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(mContext, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }
}