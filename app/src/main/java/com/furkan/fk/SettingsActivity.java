package com.furkan.fk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    byte[] thumb_byte;
    private DatabaseReference usersDatabaseReference;
    private FirebaseUser current_user;
    private CircleImageView settingsImage;
    private TextView settings_display_name, settings_status;
    private Button settings_change_image, settings_change_status;
    private StorageReference imageStorage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsImage = findViewById(R.id.settings_circleImage);
        settings_display_name = findViewById(R.id.settings_display_name);
        settings_status = findViewById(R.id.settings_status);
        settings_change_image = findViewById(R.id.settings_change_circleImage);
        settings_change_status = findViewById(R.id.settings_change_status);

        imageStorage = FirebaseStorage.getInstance().getReference();

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();

        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        usersDatabaseReference.keepSynced(true);


        usersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("display_name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                settings_display_name.setText(name);
                settings_status.setText(status);

                if (!image.equals("default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.default_avatar).into(settingsImage, new Callback() {
                        @Override
                        public void onSuccess() {


                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(image)
                                    .placeholder(R.mipmap.default_avatar).into(settingsImage);

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settings_change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String current_status = settings_status.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("current_status", current_status);
                startActivity(statusIntent);

            }
        });

        settings_change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start picker to get image for cropping and then use the image in cropping activity
                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                        */

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image...");
                progressDialog.setMessage("Please wait while we upload and process the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filepath = new File(resultUri.getPath());

                String current_uid = current_user.getUid();

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    thumb_byte = byteArrayOutputStream.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                final StorageReference thumb_filePath = imageStorage.child("profile_images").child("thumbs").child(current_uid + ".jpg");
                StorageReference filepath = imageStorage.child("profile_images").child(current_uid + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {

                                    String thumb_downloadUrl = thumbtask.getResult().getDownloadUrl().toString();

                                    if (thumbtask.isSuccessful()) {

                                        Map update_HashMap = new HashMap<>();
                                        update_HashMap.put("image", download_url);
                                        update_HashMap.put("thumb_image", thumb_downloadUrl);

                                        usersDatabaseReference.updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    progressDialog.hide();
                                                    Toast.makeText(SettingsActivity.this, "Success uploading", Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                        });

                                    } else {

                                        Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                    }

                                }
                            });

                            usersDatabaseReference.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        progressDialog.hide();
                                        Toast.makeText(SettingsActivity.this, "Success uploading", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        } else {

                            Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                error.getStackTrace();

            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue("true");


    }

}
