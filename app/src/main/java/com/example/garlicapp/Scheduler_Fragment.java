package com.example.garlicapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;

import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class Scheduler_Fragment extends Fragment {

    public Scheduler_Fragment() {
        // Required empty public constructor
    }

    public static Scheduler_Fragment newInstance() {
        return new Scheduler_Fragment();
    }

    private CalendarView calendarView, calendarViewDatePicker;
    private User user;
    private App app;
    private List<String> schedules = new ArrayList<>();

    private Calendar calendar;
    private Button cancelUpload, uploadButton;
    private List<String> selectedDates = new ArrayList<>(); // Declare the list
    private EditText start_time, end_time, password_sched;

    List<CalendarDay> calendarDays = new ArrayList<>();
    private CardView cardView, calendarViewCardView, datepickerCardView;
    private String time_off_start2, time_off_end2;
    private String startDate,endDate, time_on, time_off, password, time_on_start, time_on_end, time_off_start,time_off_end;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scheduler_fragment, container, false);
        calendarView = rootView.findViewById(R.id.calendarView);
        Button button = rootView.findViewById(R.id.button);
        cancelUpload = rootView.findViewById(R.id.cancelUpload);
        uploadButton = rootView.findViewById(R.id.uploadButton);
        start_time = rootView.findViewById(R.id.startTime);
        end_time = rootView.findViewById(R.id.endTime);
        password_sched = rootView.findViewById(R.id.schedulerPassword);
        Button nextButton = rootView.findViewById(R.id.nextButton);
        cardView = rootView.findViewById(R.id.cardview_setTime);
        calendarViewCardView = rootView.findViewById(R.id.cardviewCalendarView);
        datepickerCardView = rootView.findViewById(R.id.datePickerCardview);

        Button cancelButton = rootView.findViewById(R.id.cancelButton);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String minDate = sdf.format(new Date());
        calendarViewDatePicker = rootView.findViewById(R.id.calendarViewdayPick);
        AppConfiguration appConfiguration = new AppConfiguration.Builder(getString(R.string.App_id)).build();
        app = new App(appConfiguration);
        login();
        calendar = Calendar.getInstance();
        calendarViewDatePicker.setCalendarDayLayout(R.layout.calendar_layout);
        calendarView.setCalendarDayLayout(R.layout.calendar_layout);
        calendarView.setSelectionBackground(R.drawable.baseline_circle_24);
        calendarView.clearSelectedDays();
        Calendar min = Calendar.getInstance();
        try {
            min.setTime(sdf.parse(minDate));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                        (view, hourOfDay, minute1) -> {
                            Calendar selectedTime = Calendar.getInstance();
                            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedTime.set(Calendar.MINUTE, minute1);

                            // Store the chosen time in the EditText
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String selectedTimeString = sdf.format(selectedTime.getTime());
                            start_time.setText(selectedTimeString);

                            // Subtract 1 second and store in time_off_start2
                            Calendar modifiedTime = (Calendar) selectedTime.clone(); // Create a copy
                            modifiedTime.add(Calendar.SECOND, -1);
                            time_off_start2 = sdf.format(modifiedTime.getTime());
                            Log.d("Time", "Original time: " + selectedTimeString);
                            Log.d("Time", "Modified time: " + time_off_start2);
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });



        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                        (view, hourOfDay, minute1) -> {
                            Calendar selectedTime = Calendar.getInstance();
                            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedTime.set(Calendar.MINUTE, minute1);

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String selectedTimeString = sdf.format(selectedTime.getTime());
                            end_time.setText(selectedTimeString);
                        }, hour, minute, true);
                timePickerDialog.show();

            }
        });
        calendarViewDatePicker.setMinimumDate(min);
        calendarView.setMinimumDate(min);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewAnimation(calendarViewCardView, View.GONE);
                cardViewAnimation(datepickerCardView, View.VISIBLE);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               cardViewAnimation(calendarViewCardView, View.VISIBLE);
               cardViewAnimation(datepickerCardView, View.GONE);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (calendarViewDatePicker.getSelectedDates().isEmpty()){
                   Toast.makeText(getActivity(), "Please Select the Date", Toast.LENGTH_LONG).show();
               }else{
                   for (Calendar calendar: calendarViewDatePicker.getSelectedDates()){
                       selectedDates.add(dateFormat.format(calendar.getTime()));
                   }

                   cardViewAnimation(datepickerCardView, View.GONE);
                   cardViewAnimation(cardView, View.VISIBLE);
               }
            }
        });
        cancelUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewAnimation(cardView, View.GONE);
                cardViewAnimation(datepickerCardView, View.VISIBLE);
            }
        });



        return rootView;
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

    private void login(){
        app.loginAsync(Credentials.anonymous(), result -> {
           if (result.isSuccess()){
               user = app.currentUser();
               Executor_Mqtt.executeInBackground(new Runnable() {
                   @Override
                   public void run() {
                       new Handler(Looper.getMainLooper()).post(()->{
                           UpdateCalendar();
                       });
                   }
               });
           }
        });
    }

    private void UpdateCalendar(){
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("schedule");

        Document sort = new Document().append("start_date", -1);
        collection.find().sort(sort).limit(1).iterator().getAsync(result -> {
            if (result.isSuccess()){
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
                Calendar timeS = Calendar.getInstance();
                Calendar timeE = Calendar.getInstance();
                for (MongoCursor<Document> cursor = result.get(); cursor.hasNext();){
                    Document document = cursor.next();
                    startDate = document.getString("start_date");
                    endDate = document.getString("end_date");
                    time_on = document.getString("time_on");
                    time_off = document.getString("time_off");
                    password = document.getString("password");
                    time_on_start = document.getString("time_on_start");
                    time_on_end = document.getString("time_on_end");
                    time_off_start = document.getString("time_off_start");
                    time_off_end = document.getString("time_off_end");
                }

                      try {
                          timeS.setTime(dateFormat.parse(startDate));
                          timeE.setTime(dateFormat.parse(endDate));
                          calendar.setTime(dateFormat.parse(startDate));
                      } catch (ParseException e) {
                          throw new RuntimeException(e);
                      }
                      long dayDifference = Math.abs(timeE.getTimeInMillis() - timeS.getTimeInMillis());
                      long difference = dayDifference/(1000*60*60*24);
                      for (int i=0; i < difference+1; i++){
                          CalendarDay eventDay = new CalendarDay(calendar);
                          eventDay.setSelectedLabelColor(R.color.deep_green);
                          if (getActivity() != null){
                              eventDay.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.baseline_circle_24));
                          }
                          calendarDays.add(eventDay);
                          calendar = (Calendar) calendar.clone(); // Create a new instance of Calendar for each day
                          calendar.add(Calendar.DAY_OF_MONTH, 1);
                      }
                calendarView.setCalendarDays(calendarDays);
            }
        });
    }
    private void uploadSched(){
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("schedule");
        Document insert = new Document(new Document().append("start_date", selectedDates.get(0))
                .append("end_date", selectedDates.get((selectedDates.size()-1))));

    }

}
