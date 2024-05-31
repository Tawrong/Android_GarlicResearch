package com.example.garlicapp;
//Create new User

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class SetUsername extends AppCompatActivity {
    EditText getUser, getPassword, getEmail, getIncCode;
    TextView welcomemessage;
    GoogleSignInOptions gso;
    private String email;
    GoogleSignInClient gsc;
    private Button confirm_btn;
    boolean empty = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        String inviteCode = "G49glis12!3#";
        welcomemessage = findViewById(R.id.welcomemessage);

        getUser = findViewById(R.id.getUser);
        getEmail = findViewById(R.id.getEmail);
        getPassword = findViewById(R.id.getPassword);
        getIncCode = findViewById(R.id.getInvCode);
        if (acc != null) {
            welcomemessage.setText("Welcome, "+acc.getDisplayName());
            getEmail.setText(acc.getEmail());
            getUser.setText(acc.getDisplayName());
        }


        confirm_btn = findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String getemail = getEmail.getText().toString();
                String getpass = getPassword.getText().toString();
                String getuser = getUser.getText().toString();
                String getinv = getIncCode.getText().toString();


                if (getemail.trim().isEmpty()) {
                    empty = true;
                    getEmail.setError("This field is required");
                }
                if (getuser.trim().isEmpty()) {
                    empty = true;
                    getUser.setError("This field is required");
                }
                if (getpass.trim().isEmpty()) {
                    getPassword.setError("This field is required");
                    if (getpass.length() < 8){
                        getPassword.setError("Password Must Be Greater Than 8 Characters");
                        empty = true;
                    }
                }
                Realm.init(getApplicationContext());
                AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
                App app = new App(appConfiguration);
                app.loginAsync(Credentials.anonymous(), it -> {
                    if (it.isSuccess()) {
                        User user = app.currentUser();
                        if (user != null) {
                            MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
                            String collectionname = "users";
                            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionname);
                            collection.find().iterator().getAsync(result -> {
                                if (result.isSuccess()) {
                                    boolean isnamepresent = false;
                                    for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                        Document document = its.next();
                                        email = document.getString("email");
                                        if (getemail.equals(email)) {
                                            isnamepresent = true;
                                            break;
                                        }
                                    }
                                    if (isnamepresent && empty) {
                                        Toast.makeText(getApplicationContext(), "Email Taken", Toast.LENGTH_LONG).show();
                                        getEmail.setError("This Email" + getemail + " exists!");
                                    } else {
                                        if (!getinv.equals(inviteCode)) {
                                            getIncCode.setError("Invitation Code Do not Match!");
                                        } else if (getinv.trim().isEmpty()) {
                                            getIncCode.setError("This field is required");
                                        } else if (getpass.length()<8) {
                                            getPassword.setError("Password Must Be Greater Than 8 Characters");
                                        } else {

                                            addUserToCollection(user, getuser, getemail, getpass);
                                            Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                }
                            });
                        }
                    } else {
                        Log.e("EXAMPLE", "Failed to LogIn: " + it.getError().getErrorMessage());
                    }
                });
            }
        });
    }

    private void addUserToCollection(User user, String username, String email, String password) {
        MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
        String collectionname = "users";
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionname);
        ObjectId randID = new ObjectId();

        // Use java.time to handle date and time
        Instant instant = Instant.now();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Manila"));
        Date dateformat = Date.from(zonedDateTime.toInstant());

        // Create a new user document
        Document newUserDocument = new Document()
                .append("_id", randID)
                .append("name", username)
                .append("email", email)
                .append("password", BCrypt.withDefaults().hashToString(12, password.toCharArray()))
                .append("imagesource", "null")
                .append("filename", "null")
                .append("registration_date", dateformat);

        // Insert the new user document
        collection.insertOne(newUserDocument).getAsync(result -> {
            if (result.isSuccess()) {
                Log.v("EXAMPLE", "Inserted new user: " + username);
                Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
                intent.putExtra("email_extra_users", email);
                startActivity(intent);
            } else {
                Log.e("EXAMPLE", "Failed to insert new user: " + result.getError().getErrorMessage());
            }
        });
    }

    void signout() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}