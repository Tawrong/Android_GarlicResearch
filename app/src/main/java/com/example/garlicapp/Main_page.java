package com.example.garlicapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private final Handler handler = new Handler(Looper.getMainLooper());
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private MenuItem lastSelectedItem, firstSelectedItem;
    private String names, ImageSource;
    private User user;
    GoogleSignInOptions gso;
    private MenuItem dateMenu,timeStart,timeEnd, Controls;
    private String startD, endD, timeOns, timeOff, on_end, off_end;
    boolean firstpage;
    GoogleSignInClient gsc;
    private App app;
    private AppConfiguration appConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Realm.init(this);
        appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(appConfiguration);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        loadUser(getIntent().getStringExtra("email_extra_users"));
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Bundle emptybundle = null;
        Controls = navigationView.getMenu().findItem(R.id.controls);
        dateMenu = navigationView.getMenu().findItem(R.id.DateStart);
        timeStart = navigationView.getMenu().findItem(R.id.timeOnStart);
        timeEnd = navigationView.getMenu().findItem(R.id.timeOffEnd);

        switcFragments(new HomeFragment(), emptybundle);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.home_nav);
        menuItem.setEnabled(false);
        firstpage = true;
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_open, R.string.menu_close);
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ContextCompat.getColor(this, android.R.color.black));
            actionBarDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putString("user_email_extraEdit", getIntent().getStringExtra("email_extra_users"));
                Bundle emptybundle = null;
                if (firstpage) {
                    lastSelectedItem = navigationView.getMenu().findItem(R.id.home_nav);
                }
                if (lastSelectedItem != null) {
                    lastSelectedItem.setEnabled(true);
                    firstpage = false;
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

                if (item.getItemId() == R.id.DateStart || item.getItemId() == R.id.timeOnStart|| item.getItemId() == R.id.timeOffEnd){

                }else{
                    item.setEnabled(false);
                    lastSelectedItem = item;
                    drawerLayout.closeDrawer(GravityCompat.START);

                }

                return true;
            }
        });


    }

    void signout() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(Main_page.this, MainActivity.class));
            }
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
                if (user != null) {

                    MongoDatabase mongoDatabase = user.getMongoClient(getString(R.string.servicename)).getDatabase(getString(R.string.databaseNameUser));
                    MongoCollection<Document> collection = mongoDatabase.getCollection("users");
                    Document user_email = new Document("email", email);

                    collection.find(user_email).iterator().getAsync(result -> {
                        boolean imagesource = false;
                        if (result.isSuccess()) {
                            for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                Document document = its.next();
                                names = document.getString("name");
                                ImageSource = document.getString("imagesource");
                                Log.d("EXAMPLEs", "imagesource: " + ImageSource);
                                if (ImageSource==null||ImageSource.equals("null")) {
                                    imagesource = true;
                                }

                            }
                            loadSchedule();
                            if (imagesource) {
                                ImageView user_profile = findViewById(R.id.profile);
                                TextView user_profile_name = findViewById(R.id.username);
                                user_profile_name.setText(names);
                                Drawable drawable = getResources().getDrawable(R.drawable.baseline_person_24);
                                user_profile.setImageDrawable(drawable);
                            } else {
                                handler.postDelayed(headerinfoLoad, 500);
                            }
                        }
                    });
                } else {
                    // Handle the case where the user is null
                    Toast.makeText(Main_page.this, "User not logged in", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle login failure
                Toast.makeText(Main_page.this, "Failed to log in", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private final Runnable headerinfoLoad = new Runnable() {
        @Override
        public void run() {
            ImageView user_profile_header = findViewById(R.id.profile);
            TextView user_profile_name = findViewById(R.id.username);
            user_profile_name.setText(names);
            Transformation transformation1 = new RoundedCornersTransformation(250, 10);
            Picasso.get().load(ImageSource).transform(transformation1).fit().centerCrop(100).into(user_profile_header);
        }
    };

    protected void onDestroy() {
        handler.removeCallbacks(headerinfoLoad);
        super.onDestroy();
    }

    private void loadSchedule() {
        MongoCollection<Document> collection = user.getMongoClient(getString(R.string.servicename))
                .getDatabase(getString(R.string.databaseNameUser))
                .getCollection("schedule");

        collection.find().iterator().getAsync(result -> {
            if (result.isSuccess()) {
                for (MongoCursor<Document> its = result.get(); its.hasNext();) {
                    Document document = its.next();
                    startD = document.getString("start_date");
                    endD = document.getString("end_date");
                    timeOns = document.getString("time_on_start");
                    timeOff = document.getString("time_on_end");
                    on_end = document.getString("time_off_start");
                    off_end = document.getString("time_off_end");
                }
                if (startD == null || endD == null || timeOns == null || timeOff == null || on_end == null || off_end == null) {
                    dateMenu.setTitle("Date: N/A");
                    timeStart.setTitle("Time On: N/A");
                    timeEnd.setTitle("Time Off: N/A");
                } else {
                    dateMenu.setTitle("Date: " + startD + "-" + endD);
                    timeStart.setTitle("Time On: " + timeOns + "-" + timeOff);
                    timeEnd.setTitle("Time Off: " + on_end + "-" + off_end);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(Main_page.this, MainActivity.class));
            }
        });
        super.onBackPressed();
    }
}