package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private CircleImageView circleImageViewSetUp;
    private Button buttonSave;
    private EditText editTextName;
    private FirebaseAuth firebaseAuthSetUp;

    private StorageReference storageReferenceSetUp;
    private FirebaseFirestore firestoreSetup;

    private String UId;



    private ProgressBar progressBarSetUp;

    private Uri uriIamgeProfile;
    private Toolbar toolbarSetUp;

    private boolean isPhotoSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        circleImageViewSetUp = findViewById(R.id.circleImageView);
        buttonSave = findViewById(R.id.buttonSave);

        toolbarSetUp = findViewById(R.id.set_up_toolbar);
        progressBarSetUp = findViewById(R.id.progressBar);
        progressBarSetUp.setVisibility(View.INVISIBLE);
        editTextName = findViewById(R.id.editTextName);

        setSupportActionBar(toolbarSetUp);
        getSupportActionBar().setTitle("InstaClone");

        storageReferenceSetUp = FirebaseStorage.getInstance().getReference();
        firestoreSetup = FirebaseFirestore.getInstance();
        firebaseAuthSetUp = FirebaseAuth.getInstance();

        UId = firebaseAuthSetUp.getUid();

        firestoreSetup.collection("Users").document(UId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        //get String for "name" & "imageUrl"
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");
                        //Set editTextName & ImageView
                        editTextName.setText(name);
                        uriIamgeProfile = Uri.parse(imageUrl);
                        Glide.with(SetUpActivity.this).load(imageUrl).into(circleImageViewSetUp);
                    }
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarSetUp.setVisibility(View.VISIBLE);
                String name = editTextName.getText().toString();

                StorageReference ImageRef = storageReferenceSetUp.child("Profile_pics").child(UId + ".jpg");
                if (isPhotoSelected){
                    if(!name.isEmpty() && uriIamgeProfile != null){
                        ImageRef.putFile(uriIamgeProfile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    ImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveToFireStore(task, name, uri);
                                        }
                                    });

                                }else{
                                    progressBarSetUp.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        progressBarSetUp.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity.this, "Please Select Pictures & Enter your Name !", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    saveToFireStore(null, name, uriIamgeProfile);
                }
            }
        });

        circleImageViewSetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
                        progressBarSetUp.setVisibility(View.INVISIBLE);
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetUpActivity.this);
                    }
                }else{
                    progressBarSetUp.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, "Choose your profile & Enter your Name !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("image", downloadUri.toString());

        firestoreSetup.collection("Users").document(UId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBarSetUp.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, "Profile Settings Saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpActivity.this, MainActivity.class));
                    finish();
                }else{
                    progressBarSetUp.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                uriIamgeProfile = result.getUri();
                circleImageViewSetUp.setImageURI(uriIamgeProfile);
                isPhotoSelected = true;
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}





















