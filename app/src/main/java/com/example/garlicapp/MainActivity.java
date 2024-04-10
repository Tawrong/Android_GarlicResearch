package com.example.garlicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.bson.Document;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class MainActivity extends AppCompatActivity {
    EditText email, password;

    ImageButton showpass;
    private String emails;
    TextView forgotpass, create_new_user;

    Button loginbtn;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView googlebtn;
    private LottieAnimationView loading_animation;
    String[] permission = new String[]{
            Manifest.permission.POST_NOTIFICATIONS
    };
    boolean NotificationPermission = false;
    User user;
    AppConfiguration appConfiguration;
    App app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!NotificationPermission){
            requestNotif();
        }

        Realm.init(this);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginbtn = findViewById(R.id.loginbtn);
        forgotpass = findViewById(R.id.forgotpassword);
        create_new_user = findViewById(R.id.Create_new_user);
        showpass = findViewById(R.id.showpassword);
        create_new_user.setPaintFlags(create_new_user.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotpass.setPaintFlags(forgotpass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loading_animation = findViewById(R.id.loading);

        showpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod() == null) {
                    password.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(null);
                }
                password.setSelection(password.getText().length());
            }
        });
        forgotpass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        forgotpass.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                        forgotpass.setAlpha(0.7f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        forgotpass.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                        forgotpass.setAlpha(0.6f);
                }
                return false;
            }
        });

        create_new_user.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        create_new_user.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                        create_new_user.setAlpha(0.7f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        create_new_user.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                        create_new_user.setAlpha(0.7f);
                }
                return false;
            }
        });

        create_new_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetUsername.class);
                startActivity(intent);
            }
        });
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPass.class);
                startActivity(intent);
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_animation.playAnimation();
                findViewById(R.id.loading_back).setVisibility(View.VISIBLE);
                String user_email = email.getText().toString();
                String user_password = password.getText().toString();
                if (!user_email.trim().isEmpty() && !user_password.trim().isEmpty()) {
                   AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
                   App app = new App(appConfiguration);
                    app.loginAsync(Credentials.anonymous(), it -> {
                        if (it.isSuccess()) {
                            Log.v("EXAMPLE", "Log in as Anonymous");
                            user = app.currentUser();
                            Log.e("EXAMPLE", "Error: " + it.getError());
                            if (user != null) {
                                MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
                                String collectionname = "users";
                                MongoCollection<Document> collection = mongoDatabase.getCollection(collectionname);
                                collection.find().iterator().getAsync(result -> {
                                    if (result.isSuccess()) {
                                        boolean isnamepresent = false;
                                        boolean emaileqPass = false;
                                        for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                            Document document = its.next();
                                            emails = document.getString("email");
                                            String passwords = document.getString("password");
                                            BCrypt.Result PassResult = BCrypt.verifyer().verify(user_password.toCharArray(), passwords);
                                            if (emails.equals(user_email) && PassResult.verified) {
                                                isnamepresent = true;
                                                break;
                                            }
                                        }
                                        if (isnamepresent){
                                            loading_animation.cancelAnimation();
                                            findViewById(R.id.loading_back).setVisibility(View.GONE);
                                            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                                            intent.putExtra("email_extra_users", emails);
                                            startActivity(intent);
                                        } else {
                                            loading_animation.cancelAnimation();
                                            findViewById(R.id.loading_back).setVisibility(View.GONE);
                                            password.setError("Incorrect Password");
                                        }

                                    }
                                });
                            }
                        } else {
                            loading_animation.cancelAnimation();
                            findViewById(R.id.loading_back).setVisibility(View.GONE);
                            Log.e("EXAMPLE", "Failed to LogIn: " + it.getError().getErrorMessage());
                        }
                    });
                } else {
                    loading_animation.cancelAnimation();
                    findViewById(R.id.loading_back).setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "PLease Complete the details", Toast.LENGTH_SHORT).show();
                }

            }
        });


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        googlebtn = findViewById(R.id.Google_Signin_btn);
        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            loading_animation.playAnimation();
            findViewById(R.id.loading_back).setVisibility(View.VISIBLE);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                    if (acct.getDisplayName().equals("")) {
                        startActivity(new Intent(MainActivity.this, Main_page.class));
                    } else {
                       AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
                       App app = new App(appConfiguration);
                        app.loginAsync(Credentials.anonymous(), it -> {
                            if (it.isSuccess()) {
                                Log.v("EXAMPLE", "Log in as Anonymous");
                                user = app.currentUser();
                                Log.e("EXAMPLE", "Error: " + it.getError());
                                if (user != null) {
                                    MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename))
                                            .getDatabase(getString(R.string.databaseNameUser));
                                    String collectionname = "users";
                                    MongoCollection<Document> collection = mongoDatabase.getCollection(collectionname);
                                    StringBuilder stringBuilder = new StringBuilder();

                                    collection.find().iterator().getAsync(result -> {
                                        if (result.isSuccess()) {
                                            boolean isnamepresent = false;
                                            for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                                Document document = its.next();
                                                String email = document.getString("email");
                                                stringBuilder.append("email: ").append(email).append("\n");
                                                if (acct != null && acct.getEmail() != null && acct.getEmail().equals(email)) {
                                                    isnamepresent = true;
                                                    break;
                                                }
                                            }
                                            if (isnamepresent) {
                                                loading_animation.cancelAnimation();
                                                findViewById(R.id.loading_back).setVisibility(View.GONE);
                                                Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                                                intent.putExtra("email_extra_users", acct.getEmail());
                                                startActivity(intent);
                                            } else {
                                                loading_animation.cancelAnimation();
                                                findViewById(R.id.loading_back).setVisibility(View.GONE);
                                                Intent intent = new Intent(MainActivity.this, SetUsername.class);
                                                startActivity(intent);
                                            }

                                        }
                                    });
                                }
                            } else {
                                loading_animation.cancelAnimation();
                                findViewById(R.id.loading_back).setVisibility(View.GONE);
                                Log.e("EXAMPLE", "Failed to LogIn: " + it.getError().getErrorMessage());
                            }
                        });
                    }
                } else {
                    // Sign-in failed for some reason.
                    loading_animation.cancelAnimation();
                    findViewById(R.id.loading_back).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Sign-in failed", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                // Sign-in failed due to an exception.
                loading_animation.cancelAnimation();
                findViewById(R.id.loading_back).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void signIn() {
        Intent signinintent = gsc.getSignInIntent();
        startActivityForResult(signinintent, 1000);
    }
    private void requestNotif(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                permission[0]) == PackageManager.PERMISSION_GRANTED){
            NotificationPermission = true;
        }else{
            requespermissionlauncher.launch(permission[0]);
        }
    }
    private final ActivityResultLauncher<String> requespermissionlauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranter->{
                if (isGranter){
                    NotificationPermission = true;
                }else{
                    NotificationPermission = false;
                    showpermissionDialog("Notif Permission");
                }
            });
    public void showpermissionDialog(String permission){
        new AlertDialog.Builder(
                MainActivity.this
        ).setTitle("Notification Permission").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

}

