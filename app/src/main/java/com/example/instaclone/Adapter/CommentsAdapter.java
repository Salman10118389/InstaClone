package com.example.instaclone.Adapter;

import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instaclone.Model.Comments;
import com.example.instaclone.Model.Users;
import com.example.instaclone.R;
import com.google.firebase.firestore.auth.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {
    private Activity context;
    private List<Users> usersList;
    private List<Comments> commentsList;
    public CommentsAdapter(Activity context, List<Comments> commentsList, List<Users> usersList){
        this.commentsList = commentsList;
        this.context = context;
        this.usersList = usersList;
    }
    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment, parent, false);
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comments comments = commentsList.get(position);
        holder.setmComment(comments.getComment());

        Users users = usersList.get(position);
        holder.setmUsername(users.getName());
        holder.setCircleImageView(users.getImage());

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{
        TextView mComment, mUsername;
        CircleImageView circleImageView;
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setmComment (String comment){
            mComment = mView.findViewById(R.id.textViewComment);
            mComment.setText(comment);
        }
        public void setmUsername(String username){
            mUsername = mView.findViewById(R.id.textViewUsername);
            mUsername.setText(username);
        }
        public void setCircleImageView(String profilePic){
            circleImageView = mView.findViewById(R.id.profile_pic_commenter);
            Glide.with(context).load(profilePic).into(circleImageView);
        }


    }
}
