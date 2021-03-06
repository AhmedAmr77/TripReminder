package com.FakarnyAppForTripReminder.Fakarny;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.FakarnyAppForTripReminder.Fakarny.database.Repository;
import com.FakarnyAppForTripReminder.Fakarny.database.TripData;


public class AlarmDialog extends AppCompatActivity {
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 77;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private Repository repository;

    private int ID;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private AlertDialog alarmDialog;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new Repository(getApplication());

        ID = getIntent().getIntExtra("tripData", -1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                TripData tripData = repository.getByID(ID);
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        createNotificationChannel();
                        showDialogWithSound(tripData);
                    }
                });

            }
        }).start();


    }


    public void createNotificationChannel() {
        // Create a notification manager object.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Stand up notification",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifies every 15 minutes to stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void showDialogWithSound(TripData tripData) {
        player = MediaPlayer.create(AlarmDialog.this, R.raw.let);
        player.setLooping(true);
        player.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(AlarmDialog.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.fakarny);
        builder.setMessage(getResources().getString(R.string.msg_alertDialog)+"  :   "+tripData.getTripName());
        builder.setPositiveButton(R.string.start_btn_alertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                start(tripData);
                player.stop();
                finish();

            }
        });
        builder.setNeutralButton(R.string.snooze_btn_alertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deliverNotification(AlarmDialog.this, tripData);
                player.stop();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel_btn_alertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateTrip(tripData,"cancel");
                player.stop();
                finish();

            }
        });
        builder.setCancelable(false);
        alarmDialog = builder.create();



        alarmDialog.show();

        Button positiveButton = alarmDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alarmDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = alarmDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        // Change the alert dialog buttons text and background color
        positiveButton.setTextColor(Color.parseColor("#16c79a"));
        //positiveButton.setBackgroundColor(Color.parseColor("#16c79a"));

        negativeButton.setTextColor(Color.parseColor("#d35d6e"));
        //negativeButton.setBackgroundColor(Color.parseColor("#d35d6e"));

        neutralButton.setTextColor(Color.parseColor("#0099CC"));
        //neutralButton.setBackgroundColor(Color.parseColor("#d9dab0"));
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alarmDialog.cancel();
        player.stop();
    }

    void start(TripData tripData) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + tripData.getLat_long_endPoint()));
        String title = "TripyyReminder ";
        Intent chooser = Intent.createChooser(intent, title);

        try {
            updateTrip(tripData,"done");
            showWidget(tripData.getId());
            this.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "NO APP Can Open THIS !!!", Toast.LENGTH_SHORT).show();
        }
    }

    void showWidget(int id) {
        Intent intent = new Intent(this.getApplicationContext(), FloatingViewService.class);
        intent.putExtra("tripID", id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            (this.getApplicationContext()).startService(intent);
        } else if (Settings.canDrawOverlays(this)) {
            (this.getApplicationContext()).startService(intent);
        } else {
            askPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }

    }

    private void askPermission() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName()));
        }
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    public void updateTrip(TripData tripData, String state) {
        tripData.setState(state);
        if (state.equals("done")) {
            repository.start(tripData);
        } else {
            repository.update(tripData);
        }
    }

    private void deliverNotification(Context context, TripData tripData) {
        Intent contentIntent = new Intent(context.getApplicationContext(), AlarmDialog.class);
        contentIntent.putExtra("tripData", ID);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.trip_icon_round)
                .setContentTitle(context.getResources().getString(R.string.trip_alarm_title) + tripData.getTripName())
                .setContentText(tripData.getEnaPoint())
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }

}