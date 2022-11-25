package com.example.instaclone.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instaclone.CommentsActivity;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.PostId;
import com.example.instaclone.Model.Users;
import com.example.instaclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> mList;
    private List<Users> usersList;
    private Activity context;
    private FirebaseFirestore firestorePostAdapter;
    private FirebaseAuth firebaseAuthPostAdapter;



    public PostAdapter(Activity context, List<Post> mList, List<Users> usersList){
        this.mList = mList;
        this.context = context;
        this.usersList = usersList;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);
        firestorePostAdapter = FirebaseFirestore.getInstance();
        firebaseAuthPostAdapter = FirebaseAuth.getInstance();
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = mList.get(position);
        holder.setPostPic(post.getImage());
        holder.setPostCaption(post.getCaption());

        //post.getTime
        long milliSeconds = post.getTime().getTime();
        String date  = DateFormat.format("MM/dd/yyyy", new Date(milliSeconds)).toString();
        holder.setPostTime(date);

        //getUsername & Image
        String username = usersList.get(position).getName();
        String image = usersList.get(position).getImage();

        holder.setProfilePic(image);
        holder.setPostUsername(username);

        //LikesPic
        String postId = post.PostId;
        String CurrentUserId = firebaseAuthPostAdapter.getCurrentUser().getUid();
        holder.likePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestorePostAdapter.collection("Posts/" + postId + "/Likes").document(CurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()){
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timeStamp", FieldValue.serverTimestamp());
                            firestorePostAdapter.collection("Posts/" + postId + "/Likes").document(CurrentUserId).set(likesMap);
                        }else{
                            firestorePostAdapter.collection("Posts/" + postId + "/Likes").document(CurrentUserId).delete();
                        }
                    }
                });
            }
        });

        //Likes color change

        firestorePostAdapter.collection("Posts/" + postId + "/Likes").document(CurrentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null){
                    if (value.exists()){
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.after_liked));
                    }else{
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_border_24));
                    }
                }
            }
        });

        //Likes Count
        firestorePostAdapter.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null){
                    if (!value.isEmpty()){
                        int count = value.size();
                        holder.setPostLikes(count);
                    }else{
                        holder.setPostLikes(0);
                    }
                }
            }
        });

        //Comments Implementation
        holder.CommentsPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("postId", postId);
                context.startActivity(commentIntent);
            }
        });

        if(CurrentUserId.equals(post.getUser())){
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setClickable(true);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Delete")
                            .setMessage("Are You Sure")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firestorePostAdapter.collection("Posts/" + postId + "/Likes").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                        firestorePostAdapter.collection("Posts" + postId + "/Likes").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });

                                    firestorePostAdapter.collection("Posts" + postId + "/Comments").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshotComments : task.getResult()){
                                                        firestorePostAdapter.collection("Posts" + postId + "/Comments").document(snapshotComments.getId()).delete();
                                                    }
                                                }
                                            });
                                    firestorePostAdapter.collection("Posts").document(postId).delete();
                                    mList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show();
                }
            });
        }

    }




    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView  postPic, CommentsPic, likePic, deleteButton;
        CircleImageView profilePic;
        TextView postUsername, postTime, postCaption, postLikes;
        View mView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likePic = mView.findViewById(R.id.image_likes);
            CommentsPic = mView.findViewById(R.id.comments_pic);
            deleteButton = mView.findViewById(R.id.imageViewDelete);

        }
        public void setPostPic (String urlPost){
            postPic = mView.findViewById(R.id.image_view_post);
            Glide.with(context).load(urlPost).into(postPic);
        }
        public void setProfilePic(String urlProfile){
            profilePic = mView.findViewById(R.id.profile_pic);
            Glide.with(context).load(urlProfile).into(profilePic);
        }
        public void setPostUsername (String username){
            postUsername = mView.findViewById(R.id.textViewUsername);
            postUsername.setText(username);
        }
        public void setPostTime (String Date){
            postTime = mView.findViewById(R.id.textViewDate);
            postTime.setText(Date);
        }
        public void setPostCaption (String caption){
            postCaption = mView.findViewById(R.id.textViewCaption);
            postCaption.setText(caption);
        }

        public void setPostLikes (int count){
            postLikes = mView.findViewById(R.id.textViewLikes);
            postLikes.setText(count + " Likes");
        }
    }
}
