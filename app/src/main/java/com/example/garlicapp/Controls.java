package com.example.garlicapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.king.view.arcseekbar.ArcSeekBar;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Controls#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Controls extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Controls() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Controls.
     */
    // TODO: Rename and change types and number of parameters
    public static Controls newInstance(String param1, String param2) {
        Controls fragment = new Controls();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private CardView rack1Button, rack2Button, rack3Button, rack4Button;
    private boolean rack1State, rack2State, rack3State, rack4State = false;
    private Thread thread;
    private User user;
    private App app;
    private Integer rackNum;
    private boolean lightState = false;
    private MqttClient mqttClient;
    private ArcSeekBar seekBar;
    String val = "";
    Integer current = 0;
    private ImageView rack1View, rack2View, rack3View, rack4View;
    private TextView racktv1, racktv2, racktv3, racktv4;
    private ConstraintLayout cl1, cl2, cl3, cl4;
    private Realm realmConnect;
    private RealmResults<light_state> light_states;
    private RealmResults<aircon_tmp> aircon_tmps;
    private Boolean Rack1Clickable = true, Rack2Clickable = true, Rack3Clickable = true, Rack4Clickable = true;
    private int rack1cooldown = 1, rack2cooldown = 1, rack3cooldown = 1, rack4cooldown = 1;
    private int cooldown = 6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() != null) {
            Realm.init(requireContext());
        }

        View rootview = inflater.inflate(R.layout.fragment_controls, container, false);
        initializeViews(rootview);
        AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(appConfiguration);
        ArrayList<String> topics = new ArrayList<>();
        topics.add("garlicgreenhouse/rack1state");
        topics.add("garlicgreenhouse/rack2state");
        topics.add("garlicgreenhouse/rack3state");
        topics.add("garlicgreenhouse/rack4state");
        topics.add("garlicgreenhouse/ac_state");
        login();
        new Thread(new Runnable() {
            @Override
            public void run() {
                subscribeToTopics(topics);
            }
        }).start();

        return rootview;
    }

    private void RealmUpdate(User user) {
        if (user != null) {
            SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user)
                    .initialSubscriptions(new SyncConfiguration.InitialFlexibleSyncSubscriptions() {
                        @Override
                        public void configure(Realm realm, MutableSubscriptionSet subscriptions) {
                            try {
                                Subscription topic = subscriptions.find("RackSwitches");
                                Subscription ACtopic = subscriptions.find("ACSwitches");
                                if (topic != null) {
                                    subscriptions.remove(topic);
                                }
                                if (ACtopic != null) {
                                    subscriptions.remove(ACtopic);
                                }
                                subscriptions.add(Subscription.create("ACSwitches", realm.where(aircon_tmp.class)));
                                subscriptions.add(Subscription.create("RackSwitches", realm.where(light_state.class)));
                            } catch (Exception e) {
                                Log.e("RealmSubscriptionError", "Error configuring initial subscriptions", e);
                            }
                        }
                    })
                    .build();

            Realm.getInstanceAsync(syncConfiguration, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    realmConnect = realm;
                    light_states = realm.where(light_state.class).findAllAsync();
                    aircon_tmps = realm.where(aircon_tmp.class).equalTo("status", true).limit(1).findAllAsync();
                    light_states.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<light_state>>() {
                        @Override
                        public void onChange(RealmResults<light_state> results, OrderedCollectionChangeSet changeSet) {
                            for (light_state s : light_states) {
                                Integer racks = s.getRelayNum();

                                if (racks == 1) {
                                    Boolean state = s.getState();
                                    rack1State = state;
                                    if (Rack1Clickable) {
                                        Rack1Clickable = false;
                                        colorInvert(cl1, rack1State, rack1View, rack1cooldown);
                                    }
                                    Rack1Clickable = true;
                                    rack1cooldown = 1;
                                }
                                if (racks == 2) {
                                    Boolean state = s.getState();
                                    rack2State = state;
                                    if (Rack2Clickable) {
                                        Rack2Clickable = false;
                                        colorInvert(cl2, rack2State, rack2View, rack2cooldown);
                                    }
                                    Rack2Clickable = true;
                                    rack2cooldown = 1;
                                }
                                if (racks == 3) {
                                    Boolean state = s.getState();
                                    rack3State = state;
                                    if (Rack3Clickable) {
                                        Rack3Clickable = false;
                                        colorInvert(cl3, rack3State, rack3View, rack3cooldown);
                                    }
                                    Rack3Clickable = false;
                                    rack3cooldown = 1;
                                }
                                if (racks == 4) {
                                    Boolean state = s.getState();
                                    rack4State = state;
                                    if (Rack4Clickable) {
                                        Rack4Clickable = false;
                                        colorInvert(cl4, rack4State, rack4View, rack4cooldown);
                                    }
                                    Rack4Clickable = true;
                                    rack4cooldown = 1;
                                }
                                Log.e(String.valueOf(racks), s.getState().toString());
                            }
                        }
                    });

                    aircon_tmps.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<aircon_tmp>>() {
                        @Override
                        public void onChange(RealmResults<aircon_tmp> results, OrderedCollectionChangeSet changeSet) {
                            for (aircon_tmp a : aircon_tmps) {
                                Long temp = a.getCTemp();

                                Log.e("Temperature", temp.toString());
                            }
                        }

                    });


                }

                @Override
                public void onError(Throwable exception) {
                    super.onError(exception);
                    Log.e("RealmError", "Error initializing Realm", exception);
                }
            });
        } else {
            Log.e("RealmUpdateError", "User is null");
        }
    }


    private void colorInvert(ConstraintLayout cv, Boolean state, ImageView imageView, int cooldownTime) {
        rack1Button.setEnabled(false);
        rack2Button.setEnabled(false);
        rack3Button.setEnabled(false);
        rack4Button.setEnabled(false);
        String r1 = "Rack A";
        String r2 = "Rack B";
        String r3 = "Rack C";
        String r4 = "Rack D";
        if (state) {
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    cv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.control_buttons_design));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_green_24));
                });
            }
        } else {
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    cv.setBackground(ContextCompat.getDrawable(getContext(), R.color.white));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_24));
                });
            }
        }
        AtomicInteger time = new AtomicInteger(cooldownTime);
        Handler innerHandler = new Handler();
        int cdTIme = cooldown + 1;
        AtomicInteger countDown = new AtomicInteger(cdTIme);
        Runnable innerRunnable = new Runnable() {
            @Override
            public void run() {
                countDown.decrementAndGet();
                racktv1.setText(String.valueOf(countDown.get()));
                racktv2.setText(String.valueOf(countDown.get()));
                racktv3.setText(String.valueOf(countDown.get()));
                racktv4.setText(String.valueOf(countDown.get()));
                Log.e("CooldownTime", String.valueOf(time.get()));
                time.incrementAndGet();
                if (time.get() <= cooldown) {
                    innerHandler.postDelayed(this, 1000);
                }
                if (time.get() == cooldown + 1) {
                    rack1Button.setEnabled(true);
                    rack2Button.setEnabled(true);
                    rack3Button.setEnabled(true);
                    rack4Button.setEnabled(true);
                    racktv1.setText(r1);
                    racktv2.setText(r2);
                    racktv3.setText(r3);
                    racktv4.setText(r4);
                }
            }
        };
        innerHandler.post(innerRunnable);
    }


    public static String converttoString(Object value) {
        if (value instanceof Double || value instanceof Long || value instanceof Integer) {
            return String.valueOf(value);
        } else {
            return null;
        }

    }

    private void login() {
        app.loginAsync(Credentials.anonymous(), result -> {
            if (result.isSuccess()) {
                user = app.currentUser();
                RealmUpdate(user);
                getCurrentTemp();
                MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                        .getDatabase("GarlicGreenhouse").getCollection("light_state");
                collection.find().iterator().getAsync(result1 -> {
                    if (result1.isSuccess()) {
                        for (MongoCursor<Document> cursor = result1.get(); cursor.hasNext(); ) {
                            Document document = cursor.next();
                            rackNum = document.getInteger("relay_num");
                            lightState = document.getBoolean("state");
                            if (rackNum.equals(1)) {
                                rack1State = lightState;
                                colorInvertion(cl1, rack1State, rack1View);

                            }
                            if (rackNum.equals(2)) {
                                rack2State = lightState;
                                colorInvertion(cl2, rack2State, rack2View);
                            }
                            if (rackNum.equals(3)) {
                                rack3State = lightState;
                                colorInvertion(cl3, rack3State, rack3View);

                            }
                            if (rackNum.equals(4)) {
                                rack4State = lightState;
                                colorInvertion(cl4, rack4State, rack4View);
                            }
                            Log.d("String", String.valueOf(rackNum) + lightState);
                        }
                    }
                });
            }
        });
    }


    private void updateLightState(Realm realm, Integer relay, Boolean newState) {
        if (realm != null) {
            realm.executeTransactionAsync(r -> {
                light_state stateToUpdate = r.where(light_state.class).equalTo("relay_num", relay).findFirst();
                if (stateToUpdate != null) {
                    stateToUpdate.setState(newState);
                }
            }, () -> {
                // Transaction was a success
                Log.d("RealmUpdate", "Update successful");
            }, error -> {
                // Transaction failed and was automatically canceled
                Log.e("RealmUpdate", "Update failed", error);
            });
        } else {
            Log.e("RealmNull", "Realm is not Connected");
        }
    }


    private void getCurrentTemp() {

        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("aircon_tmp");
        Document filter = new Document("status", true);
        collection.find(filter).limit(1).iterator().getAsync(result -> {
            if (result.isSuccess()) {
                for (MongoCursor<Document> cursor = result.get(); cursor.hasNext(); ) {
                    Document document = cursor.next();
                    Long temp = document.getLong("c_temp");
                    val = temp.toString();
                    Integer ac = Integer.parseInt(val);
                    Log.e("adadad", val);
                    Integer cget = 17;
                    if (ac == 17) {
                        cget = 0;
                    } else if (ac == 18) {
                        cget = 1;
                    } else if (ac == 19) {
                        cget = 2;
                    } else if (ac == 20) {
                        cget = 3;
                    } else if (ac == 21) {
                        cget = 4;
                    } else if (ac == 22) {
                        cget = 5;
                    } else if (ac == 23) {
                        cget = 6;
                    } else if (ac == 24) {
                        cget = 7;
                    } else if (ac == 25) {
                        cget = 8;
                    } else if (ac == 26) {
                        cget = 9;
                    } else if (ac == 27) {
                        cget = 10;
                    } else if (ac == 28) {
                        cget = 11;
                    } else if (ac == 29) {
                        cget = 12;
                    } else if (ac == 30) {
                        cget = 13;
                    }

                    seekBar.showAnimation(cget, 2000);
                }
            }
        });
        Log.e("ArcSeekbar", String.valueOf(seekBar.getProgress()) + val);
    }


    private void subscribeToTopics(ArrayList<String> topics) {
        final String broker = "tcp://broker.hivemq.com:1883"; // MQTT broker address
        final String clientId = MqttClient.generateClientId();

        try {
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("Mqtt Disconnect", cause.toString());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    message.setRetained(false);
                    rack1cooldown = 1;
                    rack2cooldown = 1;
                    rack3cooldown = 1;
                    rack4cooldown = 1;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (topic.equals("garlicgreenhouse/rack1state")) {
                            String received = message.toString();
                            if (received.equals("1.0")) {
                                rack1State = true;
                            } else {
                                rack1State = false;
                            }
                            if (Rack1Clickable) {
                                Rack1Clickable = false;
                                colorInvert(cl1, rack1State, rack1View, rack1cooldown);
                            }
                            Rack1Clickable = true;
                            rack1cooldown = 1;
                        }
                        if (topic.equals("garlicgreenhouse/rack2state")) {
                            String received = message.toString();
                            if (received.equals("1.0")) {
                                rack2State = true;
                            } else {
                                rack2State = false;
                            }
                            if (Rack2Clickable) {
                                Rack2Clickable = false;
                                colorInvert(cl2, rack2State, rack2View, rack2cooldown);
                            }
                            Rack2Clickable = true;
                            rack2cooldown = 1;

                        }
                        if (topic.equals("garlicgreenhouse/rack3state")) {
                            String received = message.toString();
                            if (received.equals("1.0")) {
                                rack3State = true;
                            } else {
                                rack3State = false;
                            }
                            if (Rack3Clickable) {
                                Rack3Clickable = false;
                                colorInvert(cl3, rack3State, rack3View, rack3cooldown);
                            }
                            Rack3Clickable = true;
                            rack3cooldown = 1;

                        }
                        if (topic.equals("garlicgreenhouse/rack4state")) {
                            String received = message.toString();
                            if (received.equals("1.0")) {
                                rack4State = true;
                            } else {
                                rack4State = false;
                            }
                            if (Rack4Clickable) {
                                Rack4Clickable = false;
                                colorInvert(cl4, rack4State, rack4View, rack4cooldown);
                            } else {

                            }
                            Rack4Clickable = true;
                            rack4cooldown = 1;

                        }
                        if (topic.equals("garlicgreenhouse/ac_state")) {
                            String received = message.toString();
                            double acs = Double.parseDouble(received);
                            int ac = (int) acs;
                            Log.e("AC", String.valueOf(ac));
                            Integer cget = 0;
                            if (ac == 17) {
                                cget = 0;
                            } else if (ac == 18) {
                                cget = 1;
                            } else if (ac == 19) {
                                cget = 2;
                            } else if (ac == 20) {
                                cget = 3;
                            } else if (ac == 21) {
                                cget = 4;
                            } else if (ac == 22) {
                                cget = 5;
                            } else if (ac == 23) {
                                cget = 6;
                            } else if (ac == 24) {
                                cget = 7;
                            } else if (ac == 25) {
                                cget = 8;
                            } else if (ac == 26) {
                                cget = 9;
                            } else if (ac == 27) {
                                cget = 10;
                            } else if (ac == 28) {
                                cget = 11;
                            } else if (ac == 29) {
                                cget = 12;
                            } else if (ac == 30) {
                                cget = 13;
                            }

                            Integer finalCget = cget;
                            new Handler(Looper.getMainLooper()).post(() -> {
                                seekBar.showAnimation(finalCget, 2000);
                            });
                        }
                    });

                    Log.e("Mqtt Message", topic + " : " + message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle delivery completion
                }
            });

            mqttClient.connect(options);
            this.mqttClient = mqttClient;


            for (String topic : topics) {
                mqttClient.subscribe(topic, 0);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String topic, String message) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttClient.publish(topic, mqttMessage);
                Log.d("MQTT", "Message published to topic: " + topic + ", message: " + message);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_LONG).show();
                });
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e("MQTT", "Failed to publish message: " + e.getMessage());
            }
        } else {
            Log.e("MQTT", "MQTT client is not connected or null");
        }
    }



    private void colorInvertion(ConstraintLayout cv, Boolean state, ImageView imageView) {
        if (state) {
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    cv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.control_buttons_design));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_green_24));
                });
            }
        } else {
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    cv.setBackground(ContextCompat.getDrawable(getContext(), R.color.white));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_24));
                });
            }
        }
    }

    private void updateAc_State(Realm realm, Long temp) {
        if (realm != null) {
            realm.executeTransactionAsync(r -> {
                aircon_tmp acUpdate = r.where(aircon_tmp.class).equalTo("status", true).findFirst();
                aircon_tmp toUpdate = r.where(aircon_tmp.class).equalTo("c_temp", temp).findFirst();
                if (acUpdate != null) {
                    acUpdate.setStatus(false);
                }
                if (toUpdate != null) {
                    toUpdate.setStatus(true);
                }
            }, () -> {
                Log.d("RealmUpdate", "Update successful");
            }, error -> {
                Log.e("RealmUpdate", "Update failed", error);
            });
        } else {
            Log.e("RealmNull", "Realm is not Connected");
        }
    }

    private void initializeViews(View rootview) {
        rack1View = rootview.findViewById(R.id.lightStatus1);
        rack2View = rootview.findViewById(R.id.lightStatus2);
        rack3View = rootview.findViewById(R.id.lightStatus3);
        rack4View = rootview.findViewById(R.id.lightStatus4);
        racktv1 = rootview.findViewById(R.id.racktv1);
        racktv2 = rootview.findViewById(R.id.racktv2);
        racktv3 = rootview.findViewById(R.id.racktv3);
        racktv4 = rootview.findViewById(R.id.racktv4);
        cl1 = rootview.findViewById(R.id.rack1);
        cl2 = rootview.findViewById(R.id.rack2);
        cl3 = rootview.findViewById(R.id.rack3);
        cl4 = rootview.findViewById(R.id.rack4);
        rack1Button = rootview.findViewById(R.id.Rack1Button);
        rack2Button = rootview.findViewById(R.id.Rack2Button);
        rack3Button = rootview.findViewById(R.id.Rack3Button);
        rack4Button = rootview.findViewById(R.id.Rack4Button);
        seekBar = rootview.findViewById(R.id.arckSeekbar);
        seekBar.setOnChangeListener(new ArcSeekBar.OnChangeListener() {
            @Override
            public void onStartTrackingTouch(boolean isCanDrag) {
            }

            @Override
            public void onProgressChanged(float progress, float max, boolean fromUser) {
                if (progress == 0.0) {
                    current = 17;
                } else if (progress == 1.0) {
                    current = 18;
                } else if (progress == 2.0) {
                    current = 19;
                } else if (progress == 3.0) {
                    current = 20;
                } else if (progress == 4.0) {
                    current = 21;
                } else if (progress == 5.0) {
                    current = 22;
                } else if (progress == 6.0) {
                    current = 23;
                } else if (progress == 7.0) {
                    current = 24;
                } else if (progress == 8.0) {
                    current = 25;
                } else if (progress == 9.0) {
                    current = 26;
                } else if (progress == 10.0) {
                    current = 27;
                } else if (progress == 11.0) {
                    current = 28;
                } else if (progress == 12.0) {
                    current = 29;
                } else if (progress == 13.0) {
                    current = 30;
                }

                seekBar.setLabelText(String.valueOf(current) + "°C");
            }

            @Override
            public void onStopTrackingTouch(boolean isCanDrag) {
                updateAc_State(realmConnect, current.longValue());
                String ac_current = current.toString() + ".0";
                sendMessage("garlicgreenhouse/ac_state", ac_current);

            }

            @Override
            public void onSingleTapUp() {

            }
        });
        rack1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Rack1Clickable) {
                    rack1State = !rack1State; // Toggle the state
                    if (rack1State) {
                        updateLightState(realmConnect, 1, rack1State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack1state", "1.0");
                            }
                        });

                    } else {
                        updateLightState(realmConnect, 1, rack1State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack1state", "0.0");
                            }
                        });
                    }
                    Rack1Clickable = false;
                    colorInvert(cl1, rack1State, rack1View, rack1cooldown);
                }
                Rack1Clickable = true;
                rack1cooldown = 1;

            }
        });
        rack2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Rack2Clickable) {
                    rack2State = !rack2State; // Toggle the state
                    if (rack2State) {
                        updateLightState(realmConnect, 2, rack2State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack2state", "1.0");
                            }
                        });

                    } else {
                        updateLightState(realmConnect, 2, rack2State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack2state", "0.0");
                            }
                        });
                    }
                    Rack2Clickable = false;
                    colorInvert(cl2, rack2State, rack2View, rack2cooldown);
                } else {

                }
                Rack2Clickable = true;
                rack2cooldown = 1;


            }
        });
        rack3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Rack3Clickable) {
                    rack3State = !rack3State; // Toggle the state
                    if (rack3State) {
                        updateLightState(realmConnect, 3, rack3State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack3state", "1.0");
                            }
                        });
                    } else {
                        updateLightState(realmConnect, 3, rack3State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack3state", "0.0");
                            }
                        });
                    }
                    Rack3Clickable = false;
                    colorInvert(cl3, rack3State, rack3View, rack3cooldown);
                } else {

                }
                Rack3Clickable = true;
                rack3cooldown = 1;
            }
        });

        rack4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Rack4Clickable) {
                    rack4State = !rack4State; // Toggle the state
                    Rack4Clickable = false;
                    colorInvert(cl4, rack4State, rack4View, rack4cooldown);

                    if (rack4State) {
                        updateLightState(realmConnect, 4, rack4State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack4state", "1.0");
                            }
                        });
                    } else {
                        updateLightState(realmConnect, 4, rack4State);
                        Executor_Mqtt.executeInBackground(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage("garlicgreenhouse/rack4state", "0.0");
                            }
                        });
                    }

                } else {

                }
                rack3cooldown = 1;
                Rack4Clickable = true;
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


}