package com.example.garlicapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class Main_page extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private MenuItem lastSelectedItem, firstSelectedItem;
    private User user;
    private GoogleSignInClient gsc;
    private App app;
    private String names, imageSource;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Realm.init(this);
        Bundle emptybundle = null;
        switcFragments(new HomeFragment(), emptybundle);
        AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(appConfiguration);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        handler = new Handler(Looper.getMainLooper());

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        setupNavigationDrawer(actionBar);

        // Load user data
        loadUser(getIntent().getStringExtra("email_extra_users"));

        // Initialize Google sign-in client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        // Setup navigation item selection listener
        setupNavigationItemSelectionListener();
    }

    private void setupNavigationDrawer(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_open, R.string.menu_close);
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ContextCompat.getColor(this, android.R.color.black));
            actionBarDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
        }
    }

    private void setupNavigationItemSelectionListener() {
        navigationView.setNavigationItemSelectedListener(item -> {
            Bundle bundle = new Bundle();
            bundle.putString("user_email_extraEdit", getIntent().getStringExtra("email_extra_users"));
            Bundle emptybundle = null;
            if (lastSelectedItem != null) {
                lastSelectedItem.setEnabled(true);
            }
            if (item.getItemId() == R.id.home_nav) {
                switcFragments(new HomeFragment(), emptybundle);
            } else if (item.getItemId() == R.id.control_panel) {
                switcFragments(new Scheduler_Fragment(), emptybundle);
            } else if (item.getItemId() == R.id.user_info) {
                switcFragments(new UserFragment(), bundle);
            } else if (item.getItemId() == R.id.controls) {
                switcFragments(new Controls(), bundle );
            } else if (item.getItemId() == R.id.sensor_data) {
                switcFragments(new SensorDataFrag(), emptybundle);
            } else if (item.getItemId() == R.id.signout) {
                signout();
            }

            if (item.getItemId() != R.id.DateStart && item.getItemId() != R.id.timeOnStart && item.getItemId() != R.id.timeOffEnd) {
                item.setEnabled(false);
                lastSelectedItem = item;
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            return true;
        });
    }

    private void signout() {
        gsc.signOut().addOnCompleteListener(task -> {
            finish();
            startActivity(new Intent(Main_page.this, MainActivity.class));
        });
    }

    private void switcFragments(Fragment fragment, Bundle data) {
        if (data != null) {
            fragment.setArguments(data);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void loadUser(String email) {
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                user = app.currentUser();
                loadSchedule();
                if (user != null) {
                    MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
                    MongoCollection<Document> collection = mongoDatabase.getCollection("users");
                    Document user_email = new Document("email", email);
                    collection.find(user_email).iterator().getAsync(result -> {
                        if (result.isSuccess()) {
                            for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                Document document = its.next();
                                names = document.getString("name");
                                imageSource = document.getString("imagesource");
                            }
                            loadHeaderInfo();
                        }
                    });
                } else {
                    Toast.makeText(Main_page.this, "User not logged in", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Main_page.this, "Failed to log in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHeaderInfo() {
        if (imageSource == null || imageSource.equals("null")) {
            ImageView user_profile = findViewById(R.id.profile);
            TextView user_profile_name = findViewById(R.id.username);
            user_profile_name.setText(names);
            Drawable drawable = getResources().getDrawable(R.drawable.baseline_person_24);
            user_profile.setImageDrawable(drawable);
        } else {
            handler.postDelayed(() -> {
                ImageView user_profile_header = findViewById(R.id.profile);
                TextView user_profile_name = findViewById(R.id.username);
                user_profile_name.setText(names);
                Transformation transformation1 = new RoundedCornersTransformation(250, 10);
                Picasso.get().load(imageSource).transform(transformation1).fit().centerCrop(100).into(user_profile_header);
            }, 500);
        }
    }

    private void loadSchedule() {
        MongoCollection<Document> collection = user.getMongoClient(getString(R.string.servicename))
                .getDatabase(getString(R.string.databaseNameUser))
                .getCollection("schedule");
        Document filter = new Document("_id", -1);
        collection.find().sort(filter).limit(1).iterator().getAsync(result -> {
            if (result.isSuccess()) {
                for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                    Document document = its.next();
                    String startD = document.getString("start_date");
                    String endD = document.getString("end_date");
                    String timeOns = document.getString("time_on_start");
                    String timeOff = document.getString("time_on_end");
                    String on_end = document.getString("time_off_start");
                    String off_end = document.getString("time_off_end");

                    MenuItem dateMenu = navigationView.getMenu().findItem(R.id.DateStart);
                    MenuItem timeStart = navigationView.getMenu().findItem(R.id.timeOnStart);
                    MenuItem timeEnd = navigationView.getMenu().findItem(R.id.timeOffEnd);

                    if (startD == null || endD == null || timeOns == null || timeOff == null || on_end == null || off_end == null) {
                        dateMenu.setTitle("No Reminder");
                        dateMenu.setIcon(null);
                        timeStart.setTitle("");
                        timeEnd.setTitle("");
                    } else {
                        dateMenu.setTitle("Date: " + startD + "-" + endD);
                        timeStart.setTitle("Time On: " + timeOns + "-" + timeOff);
                        timeEnd.setTitle("Time Off: " + on_end + "-" + off_end);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        gsc.signOut().addOnCompleteListener(task -> {
            startActivity(new Intent(Main_page.this, MainActivity.class));
            finish();
        });
        super.onBackPressed();
    }
}
