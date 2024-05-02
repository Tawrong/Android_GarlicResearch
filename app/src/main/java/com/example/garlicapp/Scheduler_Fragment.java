package com.example.garlicapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class Scheduler_Fragment extends Fragment implements ScheduleAdapter.OnItemClickListener {

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
    private List<Calendar> disableddays;
    private Button cancelUpload, uploadButton;
    private List<String> selectedDates = new ArrayList<>(); // Declare the list
    private EditText start_time, end_time, password_sched;

    List<CalendarDay> calendarDays = new ArrayList<>();
    List<Calendar> disabledDays = new ArrayList<>();
    private CardView cardView, calendarViewCardView, datepickerCardView, delete_event;
    private String time_off_start2, time_off_end2;
    private String startDate, endDate, time_on, time_off, password, time_on_start, time_on_end, time_off_start, time_off_end;

    boolean isEmpty = false;
    private RecyclerView recyclerView;
    private List<Scheduler_items> items = new ArrayList<>();
    private TextView toDelete, deleteEventText;
    private Button button, proceed_delete_event, cancel_delete_event;
    private ObjectId objectId;
    private ScheduleAdapter adapter;
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scheduler_fragment, container, false);
        calendarView = rootView.findViewById(R.id.calendarView);
        button = rootView.findViewById(R.id.button);
        deleteEventText = rootView.findViewById(R.id.password_event);
        cancel_delete_event = rootView.findViewById(R.id.cancel_delete_event);
        proceed_delete_event = rootView.findViewById(R.id.proceed_delete_event);
        cancelUpload = rootView.findViewById(R.id.cancelUpload);
        uploadButton = rootView.findViewById(R.id.uploadButton);
        start_time = rootView.findViewById(R.id.startTime);
        end_time = rootView.findViewById(R.id.endTime);
        password_sched = rootView.findViewById(R.id.schedulerPassword);
        Button nextButton = rootView.findViewById(R.id.nextButton);
        cardView = rootView.findViewById(R.id.cardview_setTime);
        calendarViewCardView = rootView.findViewById(R.id.cardviewCalendarView);
        datepickerCardView = rootView.findViewById(R.id.datePickerCardview);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        delete_event = rootView.findViewById(R.id.delete_event);
        toDelete = rootView.findViewById(R.id.todelete);


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
        Calendar min = Calendar.getInstance();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ScheduleAdapter(requireContext(), items, this::onItemClick);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
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
                            modifiedTime.add(Calendar.MINUTE, -1);
                            String modifiedTimeString = sdf.format(modifiedTime.getTime());
                            time_off_start2 = modifiedTimeString;
                            Log.d("Time", "Original time: " + selectedTimeString);
                            Log.d("Time", "Modified time: " + modifiedTimeString);
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

                            // Store the chosen time in the EditText
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String selectedTimeString = sdf.format(selectedTime.getTime());
                            end_time.setText(selectedTimeString);

                            // Subtract 1 second and store in time_off_start2
                            Calendar modifiedTime = (Calendar) selectedTime.clone(); // Create a copy
                            modifiedTime.add(Calendar.MINUTE, +1);
                            String modifiedTimeString = sdf.format(modifiedTime.getTime());
                            time_off_end2 = modifiedTimeString;
                            Log.d("Time", "Original time: " + selectedTimeString);
                            Log.d("Time", "Modified time: " + modifiedTimeString);
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
                if (calendarViewDatePicker.getSelectedDates().isEmpty()) {
                    Toast.makeText(getActivity(), "Please Select the Date", Toast.LENGTH_LONG).show();
                } else {
                    for (Calendar calendar : calendarViewDatePicker.getSelectedDates()) {
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
        calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener() {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay) {
                if (calendarView.getSelectedDates() == null) {
                    Log.e("EmptyDate", "No Selected");
                } else {
                    selectOneDay(calendarDay);
                }
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_time.getText().toString().trim().isEmpty()) {
                    isEmpty = true;
                    start_time.setError("Can't be Empty");
                }

                if (end_time.getText().toString().trim().isEmpty()) {
                    isEmpty = true;
                    end_time.setError("Can't be Empty"); // Fix: Set error for end_time
                }

                if (password_sched.getText().toString().trim().isEmpty()) {
                    isEmpty = true;
                    password_sched.setError("Can't be Empty"); // Fix: Set error for password_sched
                } else {
                    isEmpty = false;
                }

                if (!isEmpty) {
                    uploadSched(selectedDates.get(0), selectedDates.get(selectedDates.size() - 1),
                            start_time.getText().toString(), end_time.getText().toString(), password_sched.getText().toString(),
                            time_off_end2, time_off_start2);
                    start_time.setText("");
                    end_time.setText("");
                    password_sched.setText("");
                }
            }
        });

        return rootView;
    }



    public void selectOneDay(@NonNull CalendarDay selectedDay) {
        if (!calendarDays.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            String selectedDateString = dateFormat.format(selectedDay.getCalendar().getTime());

            Calendar selectedCalendar = selectedDay.getCalendar();

            for (CalendarDay day : calendarDays) {
                Calendar calendarDay = day.getCalendar();
                if (selectedCalendar.compareTo(calendarDay) >= 0) {
                    // The selected date is on or after this calendar day
                    String calendarDateString = dateFormat.format(calendarDay.getTime());
                    Log.e("Match", "Selected date matches a date in the list: " + selectedDateString);
                    Log.d("MatchDate", "First Date: " + calendarDateString);
                    break; // Exit the loop once the date range is identified
                }
            }
        } else {
            Log.e("DayRowClickListener", "CalendarDays list is empty");
        }
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

    private void login() {
        app.loginAsync(Credentials.anonymous(), result -> {
            if (result.isSuccess()) {
                user = app.currentUser();
                UpdateCalendar();
            }
        });
    }

    private void UpdateCalendar() {
        if (!calendarDays.isEmpty()){
            calendarDays.clear();
        }
        if (!disabledDays.isEmpty()){
            disabledDays.clear();
        }
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("schedule");

        Document sort = new Document("_id", 1);
        collection.find().sort(sort).iterator().getAsync(result -> {
            if (result.isSuccess()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
                Calendar timeS = Calendar.getInstance();
                Calendar timeE = Calendar.getInstance();
                items.clear();
                for (MongoCursor<Document> cursor = result.get(); cursor.hasNext(); ) {
                    Document document = cursor.next();
                    ObjectId objectId1 = document.getObjectId("_id");
                    startDate = document.getString("start_date");
                    endDate = document.getString("end_date");
                    time_on = document.getString("time_on");
                    time_off = document.getString("time_off");
                    password = document.getString("password");
                    time_on_start = document.getString("time_on_start");
                    time_on_end = document.getString("time_on_end");
                    time_off_start = document.getString("time_off_start");
                    time_off_end = document.getString("time_off_end");
                    String password = document.getString("password");
                    if (startDate != null) {
                        recycleViewInsertDate(objectId1,startDate, endDate, time_on, time_off, time_off_start, time_off_end, password);
                        try {
                            timeS.setTime(dateFormat.parse(startDate));
                            timeE.setTime(dateFormat.parse(endDate));
                            calendar.setTime(dateFormat.parse(startDate));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        long dayDifference = Math.abs(timeE.getTimeInMillis() - timeS.getTimeInMillis());
                        long difference = dayDifference / (1000 * 60 * 60 * 24);
                        for (int i = 0; i < difference + 1; i++) {
                            CalendarDay eventDay = new CalendarDay(calendar);
                            eventDay.setSelectedLabelColor(R.color.deep_green);
                            if (getActivity() != null) {
                                eventDay.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.baseline_circle_24));
                            }
                            calendarDays.add(eventDay);
                            disabledDays.add(calendar);
                            calendar = (Calendar) calendar.clone();
                            calendar.add(Calendar.DAY_OF_MONTH, 1);

                        }

                    } else {
                        button.setEnabled(true);
                    }
                }
                calendarView.setCalendarDays(calendarDays);
                calendarViewDatePicker.setDisabledDays(disabledDays);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void recycleViewInsertDate(ObjectId objectId, String startDate, String endDate, String timeStart, String timeEnd,
                                       String timeOffStart, String timeOffEnd, String password) {

        items.add(new Scheduler_items(objectId, startDate + "-" + endDate, timeStart + "-" + timeEnd, timeOffStart + "-" + timeOffEnd, password));
        adapter.notifyDataSetChanged();
    }

    private void uploadSched(String start, String end, String start_time, String time_off, String password
            , String time_off_end2, String time_off_start2) {
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("schedule");
        ObjectId randID = new ObjectId();
        Document insert = new Document()
                .append("_id", randID)
                .append("start_date", start)
                .append("end_date", end)
                .append("time_on", start_time)
                .append("time_off", time_off)
                .append("password", BCrypt.withDefaults().hashToString(12, password.toCharArray()))
                .append("time_on_start", start_time)
                .append("time_on_end", time_off)
                .append("time_off_start", time_off_end2)
                .append("time_off_end", time_off_start2);

        collection.insertOne(insert).getAsync(result -> {
            if (result.isSuccess()) {
                Toast.makeText(requireContext(), "SuccessFully Inserted", Toast.LENGTH_LONG).show();
                cardViewAnimation(calendarViewCardView, View.VISIBLE);
                cardViewAnimation(cardView, View.GONE);
                calendarViewDatePicker.clearSelectedDays();
                adapter.notifyDataSetChanged();
            } else {
                Log.e("UploadSChed", "Failed" + result.getError().toString());
            }
            UpdateCalendar();
        });
    }


    private void DeleteSchedule(int position, ObjectId objectId){
        cardViewAnimation(delete_event, View.VISIBLE);
        String startDate = items.get(position).getDate();
        String password = items.get(position).getPassword();
        toDelete.setText("Date: "+ startDate);

        cancel_delete_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewAnimation(delete_event, View.GONE);
                deleteEventText.setText("");
            }
        });

        proceed_delete_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toDeleteitem = items.get(position).getPassword();
                if (BCrypt.verifyer().verify(deleteEventText.getText().toString().toCharArray(), toDeleteitem).verified){
                    deleteItem(objectId);
                    deleteEventText.setText("");
                } else {
                    Log.e("Password", "Password verification failed");
                    deleteEventText.setError("Wrong Password");
                }
            }
        });
    }


    private void deleteItem(ObjectId objectId) {
        MongoCollection<Document> collection = user.getMongoClient("garlicgreenhouse")
                .getDatabase("GarlicGreenhouse").getCollection("schedule");
        collection.deleteOne(new Document("_id", objectId)).getAsync(result -> {
            if (result.isSuccess()) {
                Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                cardViewAnimation(delete_event, View.GONE);
                UpdateCalendar();
            } else {
                Log.e("DeleteItem", "Failed to delete item: " + result.getError().toString());
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        ObjectId objectId = items.get(position).getObjectId();

        DeleteSchedule(position, objectId);
    }
}
