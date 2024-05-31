package com.example.garlicapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private VideoView[] videoViews;

    String[] sliderAllTitle = {
            "Monitoring Panel",
            "Add Schedule",
            "Light And AC Control",
            "Average Sensor Data",
            "Edit Profile"
    };

    int[] sliderAllDesc = {
            R.string.descrip0,
            R.string.descrip1,
            R.string.descrip2,
            R.string.descrip4,
            R.string.descrip3

    };

    int[] vidResources = {
            R.raw.monitoring_panel,
            R.raw.addsched_f,
            R.raw.control_f,
            R.raw.sensor_data,
            R.raw.user_f
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
        this.videoViews = new VideoView[getCount()]; // Initialize VideoView array
    }

    @Override
    public int getCount() {
        return sliderAllTitle.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.slider_screen, container, false);

        VideoView sliderVideo = view.findViewById(R.id.sliderImage);
        TextView sliderTitle = view.findViewById(R.id.sliderTitle);
        TextView sliderDesc = view.findViewById(R.id.sliderDesc);

        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + vidResources[position]);
        sliderVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
            }
        });
        sliderVideo.setVideoURI(videoUri);
        sliderVideo.start();
        sliderVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
            }
        });
        sliderVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sliderVideo.start();
            }
        });

        sliderTitle.setText(sliderAllTitle[position]);
        sliderDesc.setText(sliderAllDesc[position]);

        container.addView(view);

        // Save reference to the VideoView
        videoViews[position] = sliderVideo;

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
        // Release VideoView resources when the page is destroyed
        if (videoViews[position] != null) {
            videoViews[position].stopPlayback();
            videoViews[position] = null;
        }
    }
}

