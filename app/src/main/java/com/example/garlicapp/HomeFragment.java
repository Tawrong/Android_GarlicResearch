package com.example.garlicapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.sync.SyncConfiguration;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View rootView;
    private AppConfiguration appConfiguration;
    private User user;
    private App app;
    private RecyclerView recyclerView;
    private MqttClient mqttClient;
    private RealmResults<sensor_data> sensorData;
    private Parcelable parcelableStateRecycleView;
    private LinearLayoutManager linearLayoutManager;
    private LineChart lineChart;
    private Realm realmS;
    private SyncConfiguration syncConfiguration;
    private boolean temp, humid, lumens1, lumens2, lumens3, lumens4 = false;
    private Spinner spinner;
    Object temperature, humidity, l1, l2, l3,l4;

    private String MqttMessage_Temperature = null, MqttMessage_Humidity = null, MqttMessage_lumens1 = null, MqttMessage_lumens2 = null, MqttMessage_lumens3 = null, MqttMessage_lumens4 = null;
    private List<Entry> temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), RecyclerView.HORIZONTAL, false);
        lineChart = rootView.findViewById(R.id.lineChart);
        recyclerView.setLayoutManager(linearLayoutManager);
        lineChart.setNoDataText("Loading");
        spinner = rootView.findViewById(R.id.spinner);

        appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(appConfiguration);
        recycleViewUI("", "", "", "", "", "");
        ArrayList<String> topics = new ArrayList<>();
        topics.add("garlicgreenhouse/light1");
        topics.add("garlicgreenhouse/light2");
        topics.add("garlicgreenhouse/light3");
        topics.add("garlicgreenhouse/light4");
        topics.add("garlicgreenhouse/temperature");
        topics.add("garlicgreenhouse/humidity");
        Executor_Mqtt.executeInBackground(new Runnable() {
            @Override
            public void run() {
                subscribeToTopics(topics);
            }
        });
        ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(
                getActivity(),
                R.array.Sensor_Datas,
                R.layout.spinner_design
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        temperatureSet = new ArrayList<>();
        humiditySet = new ArrayList<>();
        lumens1Set = new ArrayList<>();
        lumens2Set = new ArrayList<>();
        lumens3Set = new ArrayList<>();
        lumens4Set = new ArrayList<>();
        if (realmS != null && !realmS.isClosed()) {
            realmS.close();
        }
        loginMongoDb();
        return rootView;
    }

    private void loginMongoDb() {

        app.loginAsync(Credentials.anonymous(), result -> {
            if (result.isSuccess()) {
                user = app.currentUser();

                Executor_Mqtt.executeInBackground(new Runnable() {
                    @Override
                    public void run() {
                        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                                .getDatabase("GarlicGreenhouse").getCollection("sensor_data");

                        new Handler(Looper.getMainLooper()).post(()->{
                            Document filterDate = new Document("date", getCurrentDate());
                            Document sortTime = new Document("time", 1);
                            collection.find(filterDate).sort(sortTime).iterator().getAsync(result1 -> {
                                if (result1.isSuccess()){
                                    for (MongoCursor<Document> cursor = result1.get();cursor.hasNext();){
                                        
                                        Document document = cursor.next();
                                        temperature = document.get("temperature");
                                        humidity = document.get("humidity");
                                        l1 = document.get("lumens1");
                                        l2 = document.get("lumens2");
                                        l3 = document.get("lumens3");
                                        l4 = document.get("lumens4");

                                        if (converttoString(temperature) != null && Float.parseFloat(converttoString(temperature))>0){
                                            temperatureSet.add(new Entry(temperatureSet.size(), Float.parseFloat(converttoString(temperature))));
                                        }
                                        if (converttoString(humidity)!= null&& Float.parseFloat(converttoString(humidity))>0){
                                            humiditySet.add(new Entry(humiditySet.size(), Float.parseFloat(converttoString(humidity))));
                                        }
                                        if (converttoString(l1)!= null&& Float.parseFloat(converttoString(l1))>0){
                                            lumens1Set.add(new Entry(lumens1Set.size(), Float.parseFloat(converttoString(l1))));
                                        }
                                        if (converttoString(l2)!= null&& Float.parseFloat(converttoString(l2))>0){
                                            lumens2Set.add(new Entry(lumens2Set.size(), Float.parseFloat(converttoString(l2))));
                                        }
                                        if (converttoString(l3)!= null && Float.parseFloat(converttoString(l3))>0){
                                            lumens3Set.add(new Entry(lumens3Set.size(), Float.parseFloat(converttoString(l3))));
                                        }
                                        if (converttoString(l4)!= null&& Float.parseFloat(converttoString(l4))>0){
                                            lumens4Set.add(new Entry(lumens4Set.size(), Float.parseFloat(converttoString(l4))));
                                        }
                                    }
                                    Log.d("Array", converttoString(temperature));
                                    if (getActivity() != null){
                                        getActivity().runOnUiThread(()->{
                                            LineDataSet temperatureDataSet = new LineDataSet(temperatureSet, "Temperature");
                                            temperatureDataSet.setColor(Color.GREEN);
                                            temperatureDataSet.setValueTextColor(Color.GREEN);
                                            temperatureDataSet.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            temperatureDataSet.setDrawCircles(false);
                                            temperatureDataSet.setLineWidth(2f);
                                            temperatureDataSet.setValueTextSize(12f);

                                            LineDataSet humidityDataSet = new LineDataSet(humiditySet, "Humidity");
                                            humidityDataSet.setColor(Color.BLUE);
                                            humidityDataSet.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            humidityDataSet.setValueTextColor(Color.BLUE);
                                            humidityDataSet.setDrawCircles(false);
                                            humidityDataSet.setLineWidth(2f);
                                            humidityDataSet.setValueTextSize(12f);

                                            LineDataSet lumens1Dset = new LineDataSet(lumens1Set, "Lumens 1");
                                            humidityDataSet.setColor(Color.YELLOW);
                                            lumens1Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            lumens1Dset.setValueTextColor(Color.BLUE);
                                            lumens1Dset.setDrawCircles(false);
                                            lumens1Dset.setLineWidth(2f);
                                            lumens1Dset.setValueTextSize(12f);

                                            LineDataSet lumens2Dset = new LineDataSet(lumens2Set, "Lumens 2");
                                            lumens2Dset.setColor(Color.BLUE); // Corrected color
                                            lumens2Dset.setValueTextColor(Color.RED);
                                            lumens2Dset.setDrawCircles(false);
                                            lumens2Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            lumens2Dset.setLineWidth(2f);
                                            lumens2Dset.setValueTextSize(12f);

                                            LineDataSet lumens3Dset = new LineDataSet(lumens3Set, "Lumens 3");
                                            lumens3Dset.setColor(Color.RED);
                                            lumens3Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            lumens3Dset.setValueTextColor(Color.RED);
                                            lumens3Dset.setDrawCircles(false);
                                            lumens3Dset.setLineWidth(2f);
                                            lumens3Dset.setValueTextSize(12f);

                                            LineDataSet lumens4Dset = new LineDataSet(lumens4Set, "Lumens 4");
                                            lumens4Dset.setColor(Color.MAGENTA);
                                            lumens4Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                                            lumens4Dset.setValueTextColor(Color.MAGENTA);
                                            lumens4Dset.setDrawCircles(false);
                                            lumens4Dset.setLineWidth(2f);
                                            lumens4Dset.setValueTextSize(12f);

                                            LineData lineData = new LineData(temperatureDataSet, humidityDataSet, lumens1Dset, lumens2Dset, lumens3Dset, lumens4Dset);
                                            Legend legend = lineChart.getLegend();
                                            legend.setTextSize(7);

                                            lineChart.setData(lineData);
                                            lineChart.getDescription().setEnabled(false); // Disable chart description
                                            XAxis xAxis = lineChart.getXAxis();
                                            xAxis.setEnabled(false);
                                            xAxis.setDrawLabels(true); // Enable X-axis labels
                                            xAxis.setGranularity(1f); // Ensure only integer labels are shown on X-axis
                                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set X-axis position
                                            YAxis yAxis = lineChart.getAxisLeft();
                                            lineChart.setGridBackgroundColor(Color.RED);
                                            lineChart.getAxisRight().setEnabled(false); // Disable right Y-axis
                                            lineChart.invalidate(); // Refresh chart data
                                        });
                                    }
                                }
                            });
                        });
                    }
                });
            }
        });
    }

    private void SensorData_Graph(){

    }

    public static String converttoString(Object value) {
        if (value instanceof Double || value instanceof Long || value instanceof Integer){
            return String.valueOf(value);
        }else{
            return null;
        }

    }

    private void recycleViewUI(String temp, String humidity, String l1, String l2, String l3, String l4) {
        List<RecycleView_Item> items = new ArrayList<>();
        items.add(new RecycleView_Item("Temperature", temp, "°C"));
        items.add(new RecycleView_Item("Humidity", humidity, "%"));
        items.add(new RecycleView_Item("Rack 1", l1, "lm"));
        items.add(new RecycleView_Item("Rack 2", l2, "lm"));
        items.add(new RecycleView_Item("Rack 3", l3, "lm"));
        items.add(new RecycleView_Item("Rack 4", l4, "lm"));

        Adapter adapter = new Adapter(getActivity().getApplicationContext(), items);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutManager().onRestoreInstanceState(parcelableStateRecycleView);
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
                    // Handle connection lost
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {


                    if (topic.equals("garlicgreenhouse/light1")) {
                        MqttMessage_lumens1 = message.toString();
                    }
                    if (topic.equals("garlicgreenhouse/light2")) {
                        MqttMessage_lumens2 = message.toString();
                    }
                    if (topic.equals("garlicgreenhouse/light3")) {
                        MqttMessage_lumens3 = message.toString();
                    }
                    if (topic.equals("garlicgreenhouse/light4")) {
                        MqttMessage_lumens4 = message.toString();
                    }
                    if (topic.equals("garlicgreenhouse/temperature")) {
                        MqttMessage_Temperature = message.toString();
                    }
                    if (topic.equals("garlicgreenhouse/humidity")) {
                        MqttMessage_Humidity = message.toString();
                    }
                    parcelableStateRecycleView = recyclerView.getLayoutManager().onSaveInstanceState();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                recycleViewUI(MqttMessage_Temperature, MqttMessage_Humidity, MqttMessage_lumens1,
                                        MqttMessage_lumens2, MqttMessage_lumens3, MqttMessage_lumens4);
                            });
                        }
                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle delivery completion
                }
            });

            mqttClient.connect(options);
            this.mqttClient = mqttClient;

            // Subscribe to each topic individually
            for (String topic : topics) {
                mqttClient.subscribe(topic, 0);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realmS != null && !realmS.isClosed()) {
            realmS.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realmS != null && !realmS.isClosed()) {
            realmS.close();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
