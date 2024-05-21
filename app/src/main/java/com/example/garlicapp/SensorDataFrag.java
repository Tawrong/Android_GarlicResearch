package com.example.garlicapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class SensorDataFrag extends Fragment {

    public SensorDataFrag() {
        // Required empty public constructor
    }
    private AppConfiguration config;
    private App app;

    private LineChart lineChart;
    private Handler handler;
    private User user;
    private boolean temp, humid, lumens1, lumens2, lumens3, lumens4 = false;

    private List<Entry> temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set;

    private Spinner spinner;
    private Object temperature, humidity, l1, l2, l3,l4;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_sensor_data, container, false);
        Realm.init(getActivity());
        config = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(config);
        lineChart = rootview.findViewById(R.id.lineChart);
        spinner = rootview.findViewById(R.id.SpinnerGraph);
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
                String choices = parent.getItemAtPosition(position).toString();
                if (choices.equals("All")) {
                    temp = true;
                    humid = true;
                    lumens1 = true;
                    lumens2 = true;
                    lumens3 = true;
                    lumens4 = true;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                }
                else if (choices.equals("Temperature")) {
                    temp = true;
                    humid = false;
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = false;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                }
                else if (choices.equals("Humidity")) {
                    temp = false;
                    humid = true;
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = false;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                }
                else if (choices.equals("Lumens1")) {
                    temp = false;
                    humid = false;
                    lumens1 = true;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = false;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                } else if (choices.equals("Lumens2")) {
                    temp = false;
                    humid = false;
                    lumens1 = false;
                    lumens2 = true;
                    lumens3 = false;
                    lumens4 = false;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                } else if (choices.equals("Lumens3")) {
                    temp = false;
                    humid = false;
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = true;
                    lumens4 = false;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                } else if (choices.equals("Lumens4")) {
                    temp = false;
                    humid = false;
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = true;
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                }
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
        login();
        return rootview;
    }

    private void login(){
        app.loginAsync(Credentials.anonymous(), it->{
            temperatureSet.clear();
            humiditySet.clear();
            lumens1Set.clear();
            lumens2Set.clear();
            lumens3Set.clear();
            lumens4Set.clear();
           if (it.isSuccess()){
               user = app.currentUser();
               MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                       .getDatabase("GarlicGreenhouse").getCollection("AveragePerDay");

               new Handler(Looper.getMainLooper()).post(()->{

                   Document sortTime = new Document("_id", 1);

                   search(collection, sortTime);
               });
           }
        });
    }

    private void search(MongoCollection<Document> collection,Document sortTime){

        collection.find().sort(sortTime).iterator().getAsync(result1 -> {
            if (result1.isSuccess()){
                for (MongoCursor<Document> cursor = result1.get();cursor.hasNext();){
                    Document document = cursor.next();
                    temperature = document.get("averageTemperature");
                    humidity = document.get("averageHumidity");
                    l1 = document.get("averageLumens1");
                    l2 = document.get("averageLumens2");
                    l3 = document.get("averageLumens3");
                    l4 = document.get("averageLumens4");

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
                    SensorData_Graph(temperatureSet, humiditySet, lumens1Set, lumens2Set, lumens3Set, lumens4Set);
                }

            }
        });
    }
    public static String converttoString(Object value) {
        if (value instanceof Integer || value instanceof Double) {

            return String.valueOf(value);
        } else {

            return null;
        }
    }

    private void SensorData_Graph(List<Entry> t, List<Entry> h, List<Entry> l1, List<Entry> l2, List<Entry> l3, List<Entry> l4){
        if (getActivity() != null){

            getActivity().runOnUiThread(()->{
                if (t.isEmpty() && h.isEmpty() && l1.isEmpty()
                        && l2.isEmpty() && l3.isEmpty() && l4.isEmpty()) {
                    lineChart.setNoDataText("No Data to Show Today");
                    lineChart.setNoDataTextColor(Color.BLACK);
                } else {
                    lineChart.setNoDataText("");
                    LineDataSet temperatureDataSet = new LineDataSet(t, "Temperature");
                    temperatureDataSet.setColor(Color.GREEN);
                    temperatureDataSet.setValueTextColor(Color.RED);
                    temperatureDataSet.setValueFormatter(new DefaultAxisValueFormatter(2));
                    temperatureDataSet.setDrawCircles(false);
                    temperatureDataSet.setLineWidth(2f);
                    temperatureDataSet.setValueTextSize(12f);

                    LineDataSet humidityDataSet = new LineDataSet(h, "Humidity");
                    humidityDataSet.setColor(Color.BLUE);
                    humidityDataSet.setValueFormatter(new DefaultAxisValueFormatter(2));
                    humidityDataSet.setValueTextColor(Color.RED);
                    humidityDataSet.setDrawCircles(false);
                    humidityDataSet.setLineWidth(2f);
                    humidityDataSet.setValueTextSize(12f);

                    LineDataSet lumens1Dset = new LineDataSet(l1, "Lumens 1");
                    humidityDataSet.setColor(Color.YELLOW);
                    lumens1Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                    lumens1Dset.setValueTextColor(Color.RED);
                    lumens1Dset.setDrawCircles(false);
                    lumens1Dset.setLineWidth(2f);
                    lumens1Dset.setValueTextSize(12f);

                    LineDataSet lumens2Dset = new LineDataSet(l2, "Lumens 2");
                    lumens2Dset.setColor(Color.BLUE); // Corrected color
                    lumens2Dset.setValueTextColor(Color.RED);
                    lumens2Dset.setDrawCircles(false);
                    lumens2Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                    lumens2Dset.setLineWidth(2f);
                    lumens2Dset.setValueTextSize(12f);

                    LineDataSet lumens3Dset = new LineDataSet(l3, "Lumens 3");
                    lumens3Dset.setColor(Color.RED);
                    lumens3Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                    lumens3Dset.setValueTextColor(Color.RED);
                    lumens3Dset.setDrawCircles(false);
                    lumens3Dset.setLineWidth(2f);
                    lumens3Dset.setValueTextSize(12f);

                    LineDataSet lumens4Dset = new LineDataSet(l4, "Lumens 4");
                    lumens4Dset.setColor(Color.MAGENTA);
                    lumens4Dset.setValueFormatter(new DefaultAxisValueFormatter(2));
                    lumens4Dset.setValueTextColor(Color.RED);
                    lumens4Dset.setDrawCircles(false);
                    lumens4Dset.setLineWidth(2f);
                    lumens4Dset.setValueTextSize(12f);

                    if (temp && humid && lumens1 && lumens2 && lumens3 && lumens4) {
                        LineData lineData = new LineData(temperatureDataSet, humidityDataSet, lumens1Dset,
                                lumens2Dset, lumens3Dset, lumens4Dset);
                        lineChart.setData(lineData);
                    } else if (temp) {
                        LineData lineData = new LineData(temperatureDataSet);
                        lineChart.setData(lineData);
                    } else if (humid) {
                        LineData lineData = new LineData(humidityDataSet);
                        lineChart.setData(lineData);
                    } else if (lumens1) {
                        LineData lineData = new LineData(lumens1Dset);
                        lineChart.setData(lineData);
                    } else if (lumens2) {
                        LineData lineData = new LineData(lumens2Dset);
                        lineChart.setData(lineData);
                    } else if (lumens3) {
                        LineData lineData = new LineData(lumens3Dset);
                        lineChart.setData(lineData);
                    } else if (lumens4) {
                        LineData lineData = new LineData(lumens4Dset);
                        lineChart.setData(lineData);
                    }

                    Legend legend = lineChart.getLegend();
                    legend.setTextSize(7);
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
                }
            });
        }
    }

}