package com.qrees.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qrees.R;
import com.qrees.model.CommentsListModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abc on 12/02/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private List<CommentsListModel> commentLists;
    private Context mContext;

    public CommentsAdapter(ArrayList<CommentsListModel> commentLists, Context mContext) {
        this.commentLists = commentLists;
        this.mContext = mContext;
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_layout,parent,false);
        return new CommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.ViewHolder holder, final int position) {
        CommentsListModel comment = commentLists.get(position);

        if (comment.profileImage != null && !comment.profileImage.equals("")) {
            Picasso.with(mContext).load(comment.profileImage).into(holder.profile_image);
        }
        holder.tv_for_fullName.setText(comment.name);
        holder.tv_for_postTime.setText(comment.day);
        holder.txt_comment.setText(comment.content);

    }

    @Override
    public int getItemCount() {
        return commentLists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_image;
        TextView tv_for_fullName,tv_for_postTime,txt_comment;

        public ViewHolder(View itemView)  {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            tv_for_fullName = itemView.findViewById(R.id.tv_for_fullName);
            tv_for_postTime = itemView.findViewById(R.id.tv_for_postTime);
            txt_comment = itemView.findViewById(R.id.txt_comment);
        }
    }
}
