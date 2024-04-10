package com.example.garlicapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.bson.Document;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class UserFragment extends Fragment {
    private static final int USER_EDIT_REQUEST_CODE = 123;
    private App app;
    private TextView useremail, userdate, username;
    private ImageView image_source;
    private ImageView user_profile;
    private User user;
    String name, email, reg_date, imagesource;
    private String default_user;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_user, container, false);
        app = new App(new AppConfiguration.Builder(getString(R.string.App_id)).build());
        Button editInfo = rootview.findViewById(R.id.user_edit_btn);
        default_user = getArguments().getString("user_email_extraEdit");
        image_source = rootview.findViewById(R.id.user_profile_pic);
        username = rootview.findViewById(R.id.user_name);
        useremail = rootview.findViewById(R.id.user_email);
        userdate = rootview.findViewById(R.id.reg_date);

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserEdit.class);
                intent.putExtra("user_email_extraEdit", default_user);
                startActivityForResult(intent, USER_EDIT_REQUEST_CODE);
                Log.d("UserFragment", "Button clicked");
            }
        });
        mongoLogin(app);
        return rootview;
    }

    private void mongoLogin(App app) {
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                user = app.currentUser();
                MongoCollection<Document> collection = user.getMongoClient(getString(R.string.servicename))
                        .getDatabase(getString(R.string.databaseNameUser))
                        .getCollection("users");
                Document filter_user = new Document("email", default_user);
                collection.find(filter_user).iterator().getAsync(result -> {
                    if (result.isSuccess()){
                        for (MongoCursor<Document> its = result.get();its.hasNext();){
                            Document document = its.next();
                            email = document.getString("email");
                            name = document.getString("name");
                            reg_date = String.valueOf(document.getDate("registration_date"));
                            imagesource = document.getString("imagesource");

                        }
                        if (getActivity() != null){
                            getActivity().runOnUiThread(()->{
                                username.setText(name);
                                useremail.setText(email);
                                userdate.setText(reg_date);
                                if (imagesource == null || imagesource.equals("null")){
                                    Drawable drawable = getResources().getDrawable(R.drawable.baseline_person_24);
                                    image_source.setImageDrawable(drawable);
                                }else{
                                    Transformation transformation1 = new RoundedCornersTransformation(250, 10);
                                    Picasso.get().load(imagesource).transform(transformation1).fit().centerCrop(100).into(image_source);
                                }
                            });
                        }
                    }
                });
            }
        });
    }





}
