package com.example.garlicapp;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.bson.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class SensorDataFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SensorDataFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorDataFrag newInstance(String param1, String param2) {
        SensorDataFrag fragment = new SensorDataFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private EditText dates, dates2;

    private String filter_date, filter_date2;
    private AppConfiguration config;
    private Spinner spinner, spinner2;
    private App app;
    private User user;
    private Object temperature, humidity;
    private String db_time;
    private Document filter, filter2;
    private LineChart lineChart, lineChart2;
    private Button reset_btn;
    private Handler handler;
    private boolean temp, humid, lumens1, lumens2, lumens3, lumens4 = false;
    private Legend legend, legend2;
    private TextView temperaturedata, humiditydata, lumensData1, lumensData2, lumensData3, lumensData4;
    private MongoCollection<Document> collection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_sensor_data, container, false);
        Realm.init(getActivity());
        config = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(config);
        dates = rootview.findViewById(R.id.editTextDate);
        lineChart = rootview.findViewById(R.id.lineChart);
        lineChart2 = rootview.findViewById(R.id.lineChart2);
        spinner = rootview.findViewById(R.id.SpinnerGraph);
        spinner2 = rootview.findViewById(R.id.spinner2);

        dates2 = rootview.findViewById(R.id.editText2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.lumensData,
                R.layout.spinner_design
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setSelection(0);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choices = parent.getItemAtPosition(position).toString();
                if (choices.equals("All")) {
                    lumens1 = true;
                    lumens2 = true;
                    lumens3 = true;
                    lumens4 = true;
                } else if (choices.equals("Lumens1")) {
                    lumens1 = true;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = false;
                } else if (choices.equals("Lumens2")) {
                    lumens1 = false;
                    lumens2 = true;
                    lumens3 = false;
                    lumens4 = false;
                } else if (choices.equals("Lumens3")) {
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = true;
                    lumens4 = false;
                } else if (choices.equals("Lumens4")) {
                    lumens1 = false;
                    lumens2 = false;
                    lumens3 = false;
                    lumens4 = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.grapOptions,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choices = parent.getItemAtPosition(position).toString();
                if (choices.equals("All")) {
                    temp = true;
                    humid = true;
                } else if (choices.equals("Temperature")) {
                    humid = false;
                    temp = true;
                } else if (choices.equals("Humidity")) {
                    humid = true;
                    temp = false;
                } else {
                    temp = true;
                    humid = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        handler = new Handler();
        handler.post(sensorData);
        dates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        dates2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog2();
            }
        });

        return rootview;
    }

    private final Runnable sensorData = new Runnable() {
        @Override
        public void run() {
            getsensorData();
            handler.postDelayed(this, 5000);
        }
    };

    private void showDatePickerDialog2() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                // Do something with the selected date
                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                // You can use the selected date as needed
                // For example, update the EditText with the selected date
                dates2.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
        dates2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter_date2 = dates2.getText().toString();
//                secondGraph();
                dates2.removeTextChangedListener(this);
            }
        });
    }

    private void showDatePickerDialog() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                // Do something with the selected date
                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                // You can use the selected date as needed
                // For example, update the EditText with the selected date
                dates.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
        dates.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter_date = dates.getText().toString();
                getsensorData();
                dates.removeTextChangedListener(this);
            }
        });

    }

    class DecimalFormatValueFormatter extends ValueFormatter {
        private final DecimalFormat format;

        public DecimalFormatValueFormatter() {
            this.format = new DecimalFormat("0.00");
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return format.format(value);
        }
    }

    private void getsensorData() {
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                user = app.currentUser();
                if (user != null) {
                    collection = user.getMongoClient("garlicgreenhouse")
                            .getDatabase("GarlicGreenhouse").getCollection("sensor_data");

                    if (filter_date == null) {
                        filter = new Document();
                    } else {
                        filter = new Document("date", filter_date);
                    }
                    Document sort = new Document().append("date", 1).append("time", 1);
                    collection.find(filter).sort(sort).iterator().getAsync(result -> {
                        List<Entry> temperatureEntries = new ArrayList<>();
                        List<Entry> humidityEntries = new ArrayList<>();
                        List<Entry> lumens1Entries = new ArrayList<>();
                        List<Entry> lumens2Entries = new ArrayList<>();
                        List<Entry> lumens3Entries = new ArrayList<>();
                        List<Entry> lumens4Entries = new ArrayList<>();
                        if (result.isSuccess()) {
                            for (MongoCursor<Document> its = result.get(); its.hasNext(); ) {
                                Document document = its.next();
                                temperature = document.get("temperature");
                                humidity = document.get("humidity");
                                db_time = document.getString("time");
                                Object lumens1 = document.get("lumens1");
                                Object lumens2 = document.get("lumens2");
                                Object lumens3 = document.get("lumens3");
                                Object lumens4 = document.get("lumens4");

                                if (converttoString(temperature) != null){
                                    temperatureEntries.add(new Entry(temperatureEntries.size(), Float.parseFloat(converttoString(temperature))));
                                }
                                if (converttoString(humidity) !=null){
                                    humidityEntries.add(new Entry(humidityEntries.size(), Float.parseFloat(converttoString(humidity))));
                                }
                                if (converttoString(lumens1) != null) {
                                    lumens1Entries.add(new Entry(lumens1Entries.size(), Float.parseFloat(converttoString(lumens1))));
                                }
                                if (converttoString(lumens2) != null) {
                                    lumens2Entries.add(new Entry(lumens2Entries.size(), Float.parseFloat(converttoString(lumens2))));
                                }
                                if (converttoString(lumens3) != null) {
                                    lumens3Entries.add(new Entry(lumens3Entries.size(), Float.parseFloat(converttoString(lumens3))));
                                }
                                if (converttoString(lumens4) != null) {
                                    lumens4Entries.add(new Entry(lumens4Entries.size(), Float.parseFloat(converttoString(lumens4))));
                                }
                            }
                            Description description = new Description();
                            LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
                            temperatureDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            temperatureDataSet.setCubicIntensity(0.2f);
                            temperatureDataSet.setColor(Color.BLUE);
                            temperatureDataSet.setValueTextColor(Color.RED);
                            temperatureDataSet.setDrawCircles(false);
                            temperatureDataSet.setLineWidth(2f);
                            temperatureDataSet.setValueTextSize(12f);

                            LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
                            humidityDataSet.setColor(Color.GREEN);
                            humidityDataSet.setValueTextColor(Color.RED);
                            humidityDataSet.setDrawCircles(false);
                            humidityDataSet.setLineWidth(2f);
                            humidityDataSet.setValueTextSize(12f);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            LineDataSet lumens1DataSet = new LineDataSet(lumens1Entries, "Lumens 1");
                            lumens1DataSet.setColor(Color.BLACK);
                            lumens1DataSet.setValueTextColor(Color.RED);
                            lumens1DataSet.setDrawCircles(false);
                            lumens1DataSet.setLineWidth(2f);
                            lumens1DataSet.setValueTextSize(12f);
                            LineDataSet lumens2DataSet = new LineDataSet(lumens2Entries, "Lumens 2");
                            lumens2DataSet.setColor(Color.YELLOW);
                            lumens2DataSet.setValueTextColor(Color.RED);
                            lumens2DataSet.setDrawCircles(false);
                            lumens2DataSet.setLineWidth(2f);
                            lumens2DataSet.setValueTextSize(12f);
                            LineDataSet lumens3DataSet = new LineDataSet(lumens3Entries, "Lumens 3");
                            lumens3DataSet.setColor(Color.GREEN);
                            lumens3DataSet.setValueTextColor(Color.RED);
                            lumens3DataSet.setDrawCircles(false);
                            lumens3DataSet.setLineWidth(2f);
                            lumens3DataSet.setValueTextSize(12f);
                            LineDataSet lumens4DataSet = new LineDataSet(lumens4Entries, "Lumens 4");
                            lumens4DataSet.setColor(Color.RED);
                            lumens4DataSet.setValueTextColor(Color.GREEN);
                            lumens4DataSet.setDrawCircles(false);
                            lumens4DataSet.setLineWidth(2f);
                            lumens4DataSet.setValueTextSize(12f);

                            if (temp && humid) {
                                LineData lineData = new LineData(temperatureDataSet, humidityDataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart.setData(lineData);
                                description.setText("All Data");
                            } else if (temp) {
                                LineData lineData = new LineData(temperatureDataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart.setData(lineData);
                                description.setText("Temperature Time Chart");
                            } else if (humid) {
                                LineData lineData = new LineData(humidityDataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart.setData(lineData);
                                description.setText("Humidity Chart");
                            }
                            if (lumens1 && lumens2 && lumens3 && lumens4) {
                                LineData lineData = new LineData(lumens1DataSet, lumens2DataSet, lumens3DataSet, lumens4DataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart2.setData(lineData);
                                description.setText("All Data");
                            } else if (lumens1) {
                                LineData lineData = new LineData(lumens1DataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart2.setData(lineData);
                                description.setText("Lumens 1");
                            } else if (lumens2) {
                                LineData lineData = new LineData(lumens2DataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart2.setData(lineData);
                                description.setText("Lumens 2");
                            } else if (lumens3) {
                                LineData lineData = new LineData(lumens3DataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart2.setData(lineData);
                                description.setText("Lumens 3");
                            } else if (lumens4) {
                                LineData lineData = new LineData(lumens4DataSet);
                                lineData.setValueFormatter(new DecimalFormatValueFormatter());
                                lineChart2.setData(lineData);
                                description.setText("Lumens 1");
                            }
                            legend2 = lineChart2.getLegend();
                            XAxis xAxis = lineChart2.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setGranularity(10f);
                            xAxis.setLabelCount(5);
                            xAxis.setGranularityEnabled(true);
                            YAxis leftYAxis = lineChart2.getAxisLeft();
                            leftYAxis.setTextColor(Color.BLACK);
                            leftYAxis.setAxisMinimum(0f);
                            YAxis rightYAxis = lineChart2.getAxisRight();
                            rightYAxis.setTextColor(Color.BLACK);
                            rightYAxis.setAxisMinimum(0f);
                            legend2.setTextColor(Color.BLACK);
                            description.setTextColor(Color.BLACK);
                            lineChart2.setDescription(description);
                            lineChart2.setDrawGridBackground(true);
                            lineChart2.setPinchZoom(true);


                            legend = lineChart.getLegend();
                            XAxis xAxis2 = lineChart.getXAxis();
                            xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis2.setLabelCount(5);
                            xAxis2.setGranularity(10f);
                            xAxis2.setGranularityEnabled(true);


                            YAxis leftYAxis2 = lineChart.getAxisLeft();
                            leftYAxis2.setTextColor(Color.BLACK);
                            leftYAxis2.setAxisMinimum(0f);

                            YAxis rightYAxis2 = lineChart.getAxisRight();
                            rightYAxis2.setTextColor(Color.BLACK);
                            rightYAxis2.setAxisMinimum(0f);

                            legend.setTextColor(Color.BLACK);
                            description.setTextColor(Color.BLACK);
                            lineChart.setDescription(description);
                            lineChart.setDrawGridBackground(true);
                            lineChart.setPinchZoom(true);
                            lineChart.invalidate();
                            lineChart2.invalidate();
                        }
                    });
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}