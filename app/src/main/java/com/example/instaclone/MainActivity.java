package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;


import com.example.instaclone.Adapter.PostAdapter;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private FirebaseFirestore firestoreMain;
    private RecyclerView recyclerViewMain;
    private FloatingActionButton fabMain;
    private List<Post> list;
    private PostAdapter adapter;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private List<Users> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get connection with FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.mainToolbar);
        firestoreMain = FirebaseFirestore.getInstance();

        recyclerViewMain  = findViewById(R.id.recyclerView);
        recyclerViewMain.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerViewMain.setHasFixedSize(true);

        usersList = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new PostAdapter(MainActivity.this, list, usersList);
        recyclerViewMain.setAdapter(adapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("InstaClone");

        fabMain = findViewById(R.id.floatingActionButton);
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPostIntent = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(addPostIntent);
            }
        });
        if (firebaseAuth.getCurrentUser() != null){

            recyclerViewMain.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean isBottom = !recyclerView.canScrollVertically(1);
                    if (isBottom){
                        Toast.makeText(MainActivity.this, "Reached Bottom", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            query = firestoreMain.collection("Posts").orderBy("time", Query.Direction.DESCENDING);
            listenerRegistration = query.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    for(DocumentChange document : value.getDocumentChanges()){
                        if (document.getType() == DocumentChange.Type.ADDED){
                            String PostId = document.getDocument().getId();
                            Post post = document.getDocument().toObject(Post.class).withId(PostId);
                            String postUserId = document.getDocument().getString("user");
                            firestoreMain.collection("users").document(postUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                Users users = task.getResult().toObject(Users.class);
                                                usersList.add(users);
                                                list.add(post);
                                                adapter.notifyDataSetChanged();
                                            }else{
                                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else{
                            adapter.notifyDataSetChanged();
                        }
                    }
                    listenerRegistration.remove();
                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else{
            String currentUserId = firebaseAuth.getCurrentUser().getUid();
            firestoreMain.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            startActivity(new Intent(MainActivity.this, SetUpActivity.class));
                            finish();
                        }
                    }
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_my_profile){
            startActivity(new Intent(MainActivity.this, SetUpActivity.class));
        }else if (item.getItemId() == R.id.itemSignOut){
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}