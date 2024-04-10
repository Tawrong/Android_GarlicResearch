package com.example.garlicapp;
//Change Password for User

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.bson.Document;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class ForgotPass extends AppCompatActivity {
    EditText otpcode, newpass1, newpass2, user_email;
    Button changepass_btn, getOtp, next_page2;
    CardView firstpage, secondpage, thirdpage;
    TextView backtoSignIn, backtoverifyEmail;
    int OTPGenerator;
    boolean hasError, hasError2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        Realm.init(this);
        otpcode = findViewById(R.id.otpcode);
        user_email = findViewById(R.id.user_email);
        newpass1 = findViewById(R.id.newpass1);
        newpass2 = findViewById(R.id.newpass2);
        backtoSignIn = findViewById(R.id.backtologin);
        backtoverifyEmail = findViewById(R.id.backtoemailinput);
        getOtp = findViewById(R.id.getOtp);
        changepass_btn = findViewById(R.id.changepass_btn);
        firstpage = findViewById(R.id.firstpage);
        secondpage = findViewById(R.id.secondpage);
        thirdpage = findViewById(R.id.thirdpage);
        cardViewAnimation(firstpage, View.VISIBLE);
        next_page2 = findViewById(R.id.nextpage2);

        user_email.setImeOptions(EditorInfo.IME_ACTION_DONE);

        getOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_email.getText().toString().trim().isEmpty()) {
                    user_email.setError("This Field is Required");
                } else {
                    cardViewAnimation(firstpage, View.GONE);
                    cardViewAnimation(secondpage, View.VISIBLE);
                    Random random = new Random();
                    OTPGenerator = random.nextInt(999999 - 100000 + 1) + 1;
                    String subject = "Garlic Green House Verification Code";
                    String message = "Your Verification Code is: " + OTPGenerator + " ";
                    new EmailAsyncTask().execute(user_email.getText().toString(), subject, message);
                    Toast.makeText(getApplicationContext(), "OTP sent", Toast.LENGTH_LONG).show();
                }
            }
        });

       backtoverifyEmail.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               cardViewAnimation(secondpage, View.GONE);
               cardViewAnimation(firstpage, View.VISIBLE);
           }
       });
       backtoSignIn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), MainActivity.class);
               startActivity(intent);
           }
       });

        next_page2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpcode.getText().toString().trim().isEmpty()) {
                    otpcode.setError("This Field is required");
                    hasError = true;
                } else {
                    if (!hasError) {
                        if (otpcode.getText().toString().equals(String.valueOf(OTPGenerator))) {
                            cardViewAnimation(secondpage, View.GONE);
                            cardViewAnimation(thirdpage, View.VISIBLE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Incorrect Verification Code", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        });
        changepass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasError2 = false;
                if (newpass1.getText().toString().trim().isEmpty()) {
                    newpass1.setError("This Field is required");
                    hasError2 = true;
                }
                if (newpass2.getText().toString().trim().isEmpty()) {
                    newpass2.setError("This Field is required");
                    hasError2 = true;
                }
                if (newpass1.getText().toString().length() < 8 && newpass2.getText().toString().length() < 8) {
                    newpass1.setError("Password Must Be Greater Than 8 Characters");
                    newpass2.setError("Password Must Be Greater Than 8 Characters");
                    hasError2 = true;
                }
                if (!newpass2.getText().toString().equals(newpass1.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password Is Not Equal", Toast.LENGTH_LONG).show();
                    hasError2 = true;
                }
                if (!hasError2) {
                    AppConfiguration appconfigs = new AppConfiguration.Builder(getString(R.string.App_id)).build();
                    App app = new App(appconfigs);
                    app.loginAsync(Credentials.anonymous(), it -> {
                        User user = app.currentUser();
                        Log.d("EXAMPLE", "Test" + user);
                        updateApasswordtoCollection(user, user_email.getText().toString(), newpass2.getText().toString());
                    });
                }
            }
        });
    }

    private void cardViewAnimation(CardView cardView, int visibility) {
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
                    .setDuration(3000)
                    .start();
        } else {
            // Slide out to bottom animation
            cardView.animate()
                    .translationY(cardView.getHeight())
                    .alpha(0f)
                    .setDuration(3000)
                    .withEndAction(() -> cardView.setVisibility(View.GONE))
                    .start();
        }
    }
    private class EmailAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // Extract parameters
            String toEmail = params[0];
            String subject = params[1];
            String body = params[2];


            final String username = "garlicgreenhouse@gmail.com";
            final String password = "s q f b q h j r y m t d o m i c";

            // Set up mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Create a session with an authenticator
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                // Create a default MimeMessage object
                Message message = new MimeMessage(session);
                // Set From: header field
                message.setFrom(new InternetAddress(username));
                // Set To: header field
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                // Set Subject: header field
                message.setSubject(subject);
                // Now set the actual message
                message.setText(body);

                // Send message
                Transport.send(message);

                Log.i("Email Status", "Email sent successfully!");
                return true;

            } catch (MessagingException e) {
                Log.e("Email Status", "Failed to send email", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Handle the result, e.g., update UI based on email sending status
        }
    }

    private void updateApasswordtoCollection(User user, String email, String newPassword) {
        MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
        String collectionName = "users";
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        // Search for the user with the given email
        Document query = new Document("email", email);

        Document update = new Document("$set", new Document("password", BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())));
        collection.updateOne(query, update).getAsync(result -> {
            if (result.isSuccess()) {
                Log.v("EXAMPLE", "Password updated successfully for user with email: " + email);
                Toast.makeText(getApplicationContext(), "Password updated successfully for user: " + email, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                Log.e("EXAMPLE", "Failed to update password: " + result.getError().getErrorMessage());
            }
        });
    }
}