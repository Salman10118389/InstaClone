package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private Button buttonAddPost;
    private EditText editTextEnterCaption;
    private ImageView imageViewAddPost;
    private Uri postImageUri = null;
    private StorageReference storageReferenceAddPost;
    private FirebaseFirestore firestoreAddPost;
    private FirebaseAuth firebaseAuthAddPost;
    private String CurrentUserID;
    private ProgressBar progressBarAddPost;
    private Toolbar toolbarAddPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        editTextEnterCaption = findViewById(R.id.editTextEnterCaption);
        buttonAddPost = findViewById(R.id.add_caption_button);
        imageViewAddPost = findViewById(R.id.add_post_imageView);

        storageReferenceAddPost = FirebaseStorage.getInstance().getReference();
        firestoreAddPost = FirebaseFirestore.getInstance();
        firebaseAuthAddPost = FirebaseAuth.getInstance();
        toolbarAddPost = findViewById(R.id.add_post_toolbar);
        progressBarAddPost = findViewById(R.id.progressBar2);


        setSupportActionBar(toolbarAddPost);
        getSupportActionBar().setTitle("InstaClone");

        CurrentUserID = firebaseAuthAddPost.getCurrentUser().getUid();

        imageViewAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(3,2)
                        .setMinCropResultSize(512, 512)
                        .start(AddPostActivity.this);
            }
        });

        buttonAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarAddPost.setVisibility(View.VISIBLE);
                String caption = editTextEnterCaption.getText().toString();

                if (!caption.isEmpty() && postImageUri != null){
                    StorageReference postREF = storageReferenceAddPost.child("post_images").child(FieldValue.serverTimestamp().toString() + ".jpg");
                    postREF.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                postREF.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        HashMap<String, Object> postMap = new HashMap<>();
                                        postMap.put("image", uri.toString());
                                        postMap.put("user", CurrentUserID);
                                        postMap.put("caption", caption);
                                        postMap.put("time", FieldValue.serverTimestamp());

                                        firestoreAddPost.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    progressBarAddPost.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, "Post Added Successfully", Toast.LENGTH_SHORT).show();
                                                    Intent backToMain = new Intent(AddPostActivity.this, MainActivity.class);
                                                    startActivity(backToMain);
                                                    finish();
                                                }else{
                                                    progressBarAddPost.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                progressBarAddPost.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    progressBarAddPost.setVisibility(View.INVISIBLE);
                    Toast.makeText(AddPostActivity.this, "Please Add Image & Write your Caption to Post !", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                postImageUri = result.getUri();
                imageViewAddPost.setImageURI(postImageUri);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}