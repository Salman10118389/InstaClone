package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instaclone.Adapter.CommentsAdapter;
import com.example.instaclone.Model.Comments;
import com.example.instaclone.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    private EditText CommentsEdit;
    private Button buttonAddComments;
    private RecyclerView recyclerViewComments;
    private FirebaseFirestore firestoreComments;
    private String postId;
    private FirebaseAuth firebaseAuthComments;
    private String currentUserId;
    private CommentsAdapter commentsAdapter;
    private List<Comments> commentsListComments;
    private List<Users> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //XML with Java Connection
        CommentsEdit = findViewById(R.id.editTextComments);
        buttonAddComments = findViewById(R.id.addCommentsButton);
        firestoreComments = FirebaseFirestore.getInstance();
        recyclerViewComments = findViewById(R.id.recyclerViewComments);

        firebaseAuthComments = FirebaseAuth.getInstance();
        currentUserId = firebaseAuthComments.getCurrentUser().getUid();

        postId = getIntent().getStringExtra("postId");

        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));

        commentsListComments = new ArrayList<>();
        usersList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(CommentsActivity.this, commentsListComments, usersList);
        recyclerViewComments.setAdapter(commentsAdapter);

        //Retrieve the Data
        firestoreComments.collection("Posts/" + postId + "/Comments").addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        Comments comments = documentChange.getDocument().toObject(Comments.class);

                        String userId = documentChange.getDocument().getString("user");
                        firestoreComments.collection("Users").document(userId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            Users users = task.getResult().toObject(Users.class);
                                            usersList.add(users);
                                            
                                            commentsListComments.add(comments);
                                            commentsAdapter.notifyDataSetChanged();
                                        }else{
                                            Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }else{
                        commentsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        buttonAddComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = CommentsEdit.getText().toString();
                if (!comment.isEmpty()){
                    Map<String, Object> CommentsMap = new HashMap<>();
                    CommentsMap.put("comment", comment);
                    CommentsMap.put("time", FieldValue.serverTimestamp());
                    CommentsMap.put("user", currentUserId);
                    firestoreComments.collection("Posts/" + postId + "/Comments").add(CommentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(CommentsActivity.this, "Please Enter Your Comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}