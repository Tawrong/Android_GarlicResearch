package com.example.garlicapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class UserEdit extends AppCompatActivity {
    private String email_FB, imageSource_FB, filename_FB;
    private String user_emails, user_names, user_pass, user_profilePic, file_name;
    private String imageLinkFirebase, filename_FireBObject;
    private String names, emails, old_pass;
    private Uri imageuri;
    String UserEmail;
    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private EditText user_email, user_password1, user_password2, user_name;
    private String user_email_db, user_password_db;
    private AppConfiguration config;
    private String name, email, password;
    private App app;
    Button confirm_edit_btn, cancel_btn;
    TextView tv3, tv4;
    CheckBox show_password_btn, edit_pass;
    private User user;
    ImageButton select_profile_btn;
    ImageView user_profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);
        Realm.init(this);
        config = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(config);

        user_name = findViewById(R.id.user_name);
        user_profile_image = findViewById(R.id.user_profile_pic);
        user_email = findViewById(R.id.user_email);
        user_password1 = findViewById(R.id.user_password1);
        user_password2 = findViewById(R.id.user_password2);
        edit_pass = findViewById(R.id.edit_pass);
        select_profile_btn = findViewById(R.id.select_profile_btn);
        confirm_edit_btn = findViewById(R.id.user_edit_btn);
        show_password_btn = findViewById(R.id.switch_showpassword);
        cancel_btn = findViewById(R.id.Cancel);
        storageReference = FirebaseStorage.getInstance().getReference();

        initializeInputs(getIntent().getStringExtra("user_email_extraEdit"));
        UserEmail = getIntent().getStringExtra("user_email_extraEdit");

        show_password_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int selection = user_password1.getSelectionEnd();
                int selection2 = user_password2.getSelectionEnd();
                if (isChecked) {
                    user_password1.setTransformationMethod(null);
                    user_password2.setTransformationMethod(null);
                } else {
                    user_password1.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                    user_password2.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                }
                user_password1.setSelection(selection);
                user_password2.setSelection(selection2);
            }
        });
        edit_pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ConstraintLayout constraintLayout = findViewById(R.id.pass_animation);
                if (edit_pass.isChecked()){
                    cardViewAnimation(constraintLayout, View.VISIBLE);
                }else{
                    cardViewAnimation(constraintLayout, View.GONE);
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        confirm_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_pass = user_password1.getText().toString();
                BCrypt.Result PasswordRes = BCrypt.verifyer().verify(old_pass.toCharArray(), user_pass);
                boolean istrue = PasswordRes.verified;

                if (!istrue){
                    istrue = false;
                }

                if (!edit_pass.isChecked()){
                    infoEdit(user_name.getText().toString(), user_password2.getText().toString());
                }else{
                    if (istrue){
                        infoEdit(user_name.getText().toString(), user_password2.getText().toString());
                    }else{
                        user_password1.setError("Incorrect Password");
                    }
                }
            }
        });

        select_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageuri = data.getData();
            Log.e("EXAMPLE", imageuri.toString());
            Transformation transformation1 = new RoundedCornersTransformation(70, 0);
            Picasso.get().load(imageuri).transform(transformation1).fit().centerCrop(100).into(user_profile_image);
            firebaseUploadImage(imageuri);

        }
    }
    private void firebaseUploadImage(Uri image){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(image));
            fileReference.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getApplicationContext(), "Upload Successfully", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageLinkFirebase = uri.toString();
                            filename_FireBObject = fileReference.getName();
                            Log.v("EXAMPLE", "Img URL: " + fileReference.getDownloadUrl());
                            ImageUpload(imageLinkFirebase, filename_FireBObject);
                            writeUserData(user_emails, filename_FireBObject, imageLinkFirebase);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // if error
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeUserData(String email, String Filename, String imageSource) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        String userid = user_names;
        User_class user_class = new User_class(email, Filename, imageSource);
        reference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    reference.child(userid).setValue(user_class, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Log.d("Firebase", "User data updated successfully!");
                            } else {
                                Log.w("Firebase", "Error updating user data", error.toException());
                            }
                        }
                    });

                } else {
                    reference.child(userid).setValue(user_class, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@NonNull DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Log.d("Firebase", "User data written successfully!");
                            } else {
                                Log.w("Firebase", "Error writing user data", databaseError.toException());
                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void initializeInputs(String email) {
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                user = app.currentUser();
                MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
                MongoCollection<Document> collection = mongoDatabase.getCollection("users");
                Document find_userEmail = new Document("email", email);
                collection.find(find_userEmail).iterator().getAsync(result -> {
                    if (result.isSuccess()) {
                        for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                            Document document = its.next();
                            user_emails = document.getString("email");
                            user_names = document.getString("name");
                            user_pass = document.getString("password");
                            file_name = document.getString("filename");
                            user_profilePic = document.getString("imagesource");
                        }
                        writeUserData(user_emails, file_name, user_profilePic);
                        user_email.setText(user_emails);
                        user_name.setText(user_names);

                        if (user_profilePic==null||user_profilePic.equals("null")) {
                            Drawable drawable = getResources().getDrawable(R.drawable.baseline_person_24);
                            user_profile_image.setImageDrawable(drawable);
                        } else {
                            Transformation transformation1 = new RoundedCornersTransformation(250, 10);
                            Picasso.get().load(user_profilePic).transform(transformation1).fit().centerCrop(100).into(user_profile_image);
                        }
                    } else {
                        Log.e("EXAMPLE", "ERROR");
                    }
                });
            }
        });
    }

    private void ImageUpload(String imageLink, String filename_link) {
        MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
        MongoCollection<Document> collection = mongoDatabase.getCollection("users");
        Document find_userEmail = new Document("email", UserEmail);
        Document update = new Document("$set", new Document("imagesource", imageLink).append("filename", filename_link));
        collection.updateOne(find_userEmail, update).getAsync(result -> {
            if (result.isSuccess()) {
                Log.e("ImageUpload", "Success");
            } else {
                Log.e("ImageUpload", "Failed: " + result.getError().getErrorMessage());
            }
        });
    }

    private void infoEdit(String name, String password) {
        MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
        MongoCollection<Document> collection = mongoDatabase.getCollection("users");

        Document find_userEmail = new Document("email", UserEmail);
        if (!edit_pass.isChecked()){
            Document updatename = new Document("$set", new Document("name", name));
            collection.updateOne(find_userEmail, updatename).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.e("ImageUpload", "Success");
                    Intent intent = new Intent(getApplicationContext(), Main_page.class);
                    intent.putExtra("email_extra_users", UserEmail);
                    startActivity(intent);
                } else {
                    Log.e("ImageUpload", "Failed: " + result.getError().getErrorMessage());
                }
            });
        }else{
            Document update_pass = new Document("$set", new Document("name", name)
                    .append("password", BCrypt.withDefaults().hashToString(12, password.toCharArray())));
            collection.updateOne(find_userEmail, update_pass).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.e("ImageUpload", "Success");
                    Realm.getInstance(Realm.getDefaultConfiguration()).close();
                    Intent intent = new Intent(getApplicationContext(), Main_page.class);
                    intent.putExtra("email_extra_users", UserEmail);
                    startActivity(intent);
                } else {
                    Log.e("ImageUpload", "Failed: " + result.getError().getErrorMessage());
                }
            });
        }
    }
    private void cardViewAnimation(ConstraintLayout cardView, int visibility) {
        if (cardView.getVisibility() == visibility) {
            return;
        }

        if (visibility == View.VISIBLE) {
            // Slide in from top animation
            cardView.setTranslationY(-cardView.getHeight());
            cardView.setAlpha(0f);
            cardView.setVisibility(View.VISIBLE);

            cardView.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(1500)
                    .start();
        } else {
            // Slide out to bottom animation
            cardView.animate()
                    .translationY(cardView.getHeight())
                    .alpha(0f)
                    .setDuration(1500)
                    .withEndAction(() -> cardView.setVisibility(View.GONE))
                    .start();
        }
    }
}
