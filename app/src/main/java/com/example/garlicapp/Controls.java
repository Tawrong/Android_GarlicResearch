package com.example.garlicapp;

import android.graphics.Color;
import android.media.Image;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

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
    private ImageView rack1View,rack2View, rack3View, rack4View;
    private TextView racktv1, racktv2, racktv3, racktv4;
    private ConstraintLayout cl1, cl2, cl3, cl4;

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
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                subscribeToTopics(topics);
            }
        });
        thread.start();
        return rootview;
    }

    private void colorInvert(ConstraintLayout cv, Boolean state, ImageView imageView) {
        if (state) {
           new Handler(Looper.getMainLooper()).post(()->{
               cv.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.control_buttons_design));
               imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_green_24));
           });
        } else {
            new Handler(Looper.getMainLooper()).post(()->{
                cv.setBackground(ContextCompat.getDrawable(requireContext(), R.color.white));
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_light_mode_24));
            });
        }
    }

    private void login() {
        app.loginAsync(Credentials.anonymous(), result -> {
            if (result.isSuccess()) {
                user = app.currentUser();
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
                                colorInvert(cl1, rack1State, rack1View);

                            }
                            if (rackNum.equals(2)) {
                                rack2State = lightState;
                                colorInvert(cl2, rack2State, rack2View);
                            }
                            if (rackNum.equals(3)) {
                                rack3State = lightState;
                                colorInvert(cl3, rack3State, rack3View);

                            }
                            if (rackNum.equals(4)) {
                                rack4State = lightState;
                                colorInvert(cl4, rack4State, rack4View);

                            }
                            Log.d("String", String.valueOf(rackNum) + lightState);
                        }
                    }
                });
            }
        });
    }

    private void toggleButton(Integer relay, Boolean state) {
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("light_state");
        Document update = new Document("$set", new Document("state", state));
        Document query = new Document("relay_num", relay);

        collection.updateOne(query, update).getAsync(result -> {

        });
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

    private void toggleAcOff() {
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("aircon_tmp");
        Document document = new Document("$set", new Document("status", false));
        Document query = new Document("status", true);
        collection.updateMany(query, document).getAsync(result -> {
            if (result.isSuccess()) {
                Log.e("turned_oFf all", "Success");
            } else {
                Log.e("turned_oFf all", "Fail");
            }
        });
    }

    private void toggleAc(Long temp) {
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("aircon_tmp");
        Document update = new Document("$set", new Document("status", true));
        Document query = new Document("c_temp", temp);

        collection.updateOne(query, update).getAsync(result -> {
            if (result.isSuccess()) {
                Toast.makeText(requireContext(), "Success", Toast.LENGTH_LONG).show();
            }
        });
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

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    message.setRetained(false);
                    if (topic.equals("garlicgreenhouse/rack1state")) {
                        String received = message.toString();
                        if (received.equals("1.0")) {
                            rack1State = true;
                        } else {
                            rack1State = false;
                        }
                        colorInvert(cl1, rack1State, rack1View);
                    }
                    if (topic.equals("garlicgreenhouse/rack2state")) {
                        String received = message.toString();
                        if (received.equals("1.0")) {
                            rack2State = true;
                        } else {
                            rack2State = false;
                        }
                        colorInvert(cl2, rack2State, rack2View);

                    }
                    if (topic.equals("garlicgreenhouse/rack3state")) {
                        String received = message.toString();
                        if (received.equals("1.0")) {
                            rack3State = true;
                        } else {
                            rack3State = false;
                        }
                        colorInvert(cl3, rack3State, rack3View);

                    }
                    if (topic.equals("garlicgreenhouse/rack4state")) {
                        String received = message.toString();
                        if (received.equals("1.0")) {
                            rack4State = true;
                        } else {
                            rack4State = false;
                        }
                        colorInvert(cl4, rack4State, rack4View);

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
                        new Handler(Looper.getMainLooper()).post(()->{
                            seekBar.showAnimation(finalCget, 2000);
                        });
                    }

                    Log.e("Mqtt Message", topic.toString() + " : " + message.toString());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (thread != null && thread.isAlive()){
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
        if (thread != null && thread.isAlive()){
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
    private void initializeViews(View rootview){
        rack1View = rootview.findViewById(R.id.lightStatus1);
        rack2View = rootview.findViewById(R.id.lightStatus2);
        rack3View = rootview.findViewById(R.id.lightStatus3);
        rack4View = rootview.findViewById(R.id.lightStatus4);
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
                toggleAcOff();
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
                String ac_current = current.toString() + ".0";
                sendMessage("garlicgreenhouse/ac_state", ac_current);
                toggleAc(current.longValue());
            }

            @Override
            public void onSingleTapUp() {

            }
        });
        rack1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rack1State = !rack1State; // Toggle the state
                if (rack1State) {
                    toggleButton(1, rack1State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack1state", "1.0");
                        }
                    });

                } else {
                    toggleButton(1, rack1State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack1state", "0.0");
                        }
                    });
                }
                colorInvert(cl1, rack1State, rack1View);
            }
        });
        rack2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rack2State = !rack2State; // Toggle the state
                if (rack2State) {
                    toggleButton(2, rack2State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack2state", "1.0");
                        }
                    });

                } else {
                    toggleButton(2, rack2State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack2state", "0.0");
                        }
                    });
                }
                colorInvert(cl2, rack2State, rack2View);
            }
        });
        rack3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rack3State = !rack3State; // Toggle the state
                if (rack3State) {
                    toggleButton(3, rack3State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack3state", "1.0");
                        }
                    });
                } else {
                    toggleButton(3, rack3State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack3state", "0.0");
                        }
                    });
                }
                colorInvert(cl3, rack3State, rack3View);
            }
        });

        rack4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rack4State = !rack4State; // Toggle the state
                if (rack4State) {
                    toggleButton(4, rack4State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack4state", "1.0");
                        }
                    });
                } else {
                    toggleButton(4, rack4State);
                    Executor_Mqtt.executeInBackground(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("garlicgreenhouse/rack4state", "0.0");
                        }
                    });
                }
                colorInvert(cl4, rack4State, rack4View);
            }
        });

    }

}