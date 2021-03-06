package com.srikanth.royal.dietg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.srikanth.royal.dietg.Prevalent.Prevalent;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class settings extends AppCompatActivity {
    private TextView changeProfileImage, close, update;
    private EditText settingName, settingPhone;
    private CircleImageView settingImage;
    private StorageTask UploadTask;

    private Uri imageUri;
    private String myUrl = "";
    private StorageReference pIRef;
    private String checker = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        changeProfileImage = (TextView) findViewById(R.id.change_profile_image);
        close = (TextView) findViewById(R.id.close);
        update = (TextView) findViewById(R.id.update);
        settingName = (EditText) findViewById(R.id.settings_name);
        settingPhone = (EditText) findViewById(R.id.settings_phone);
        settingImage = (CircleImageView) findViewById(R.id.settings_image);
        pIRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.equals("clicked")) {
                    userInfoSave();
                } else {
                    updateOnlyuserInfo();
                }
            }
        });

        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .start(settings.this);
            }
        });

        userInfoDisplay(settingImage, settingName, settingPhone);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            settingImage.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(settings.this, settings.class));
            finish();
        }
    }

    private void userInfoDisplay(final CircleImageView settingImage, final EditText settingName, final EditText settingPhone) {


        DatabaseReference uRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.CurrentonlineUser.getPhone());
        uRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("image").exists()) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        String pass = dataSnapshot.child("password").getValue().toString();
                        Picasso.get().load(image).into(settingImage);
                        settingName.setText(name);
                        settingPhone.setText(phone);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfoSave() {

        if (TextUtils.isEmpty(settingName.getText().toString())) {
            Toast.makeText(this, "Name is Required", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(settingPhone.getText().toString())) {
            Toast.makeText(this, "Phone is Required", Toast.LENGTH_SHORT).show();
        } else if (checker.equals("clicked")) {
            uploadImage();
        }

    }

    private void uploadImage() {


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileRef = pIRef
                    .child(Prevalent.CurrentonlineUser.getPhone() + ".jpg");
            UploadTask = fileRef.putFile(imageUri);
            UploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", settingName.getText().toString());
                        userMap.put("phoneOrder", settingPhone.getText().toString());
                        userMap.put("image", myUrl);
                        ref.child(Prevalent.CurrentonlineUser.getPhone()).updateChildren(userMap);
                        progressDialog.dismiss();
                        startActivity(new Intent(settings.this, start.class));
                        Toast.makeText(settings.this, "Update Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(settings.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOnlyuserInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", settingName.getText().toString());
        userMap.put("phoneOrder", settingPhone.getText().toString());
        ref.child(Prevalent.CurrentonlineUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(settings.this, start.class));
        Toast.makeText(settings.this, "Update Successful", Toast.LENGTH_SHORT).show();
        finish();

    }


}








































