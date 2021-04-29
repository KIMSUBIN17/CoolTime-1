package com.example.timerapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class BackgroundEmptyPlace extends Service {
    NotificationManager manager;
    private SharedPreferences pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);

    // 핸들러
    private Handler h;
    private Runnable r;

    // 빌더 채널 이름
    private String EMPTYCHANNEL = "IWantRest";

    private ArrayList<DatabaseManager> DB = new ArrayList<>();
    private int WASHERCOUNT = 6;

    private int startTimer = 0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate()
    {
        startTimer = 0;
        Log.e("백 그라운드", "자기 소개 하기");
        super.onCreate();
    }

    NotificationCompat.Builder builder;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Notification updateNotificationEmpty() {
        startTimer++;
        Context context = getApplicationContext();
        PendingIntent action = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        // Flag indicating that if the described PendingIntent already exists, the current one should be canceled before generating a new one.
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int count = UsingCount();

        String msg = count + "개 남았습니다.";

        if(startTimer < 4)
        {
            builder = SetBuilderEmpty(NotificationManager.IMPORTANCE_LOW, EMPTYCHANNEL);
            return builder
                    .setContentIntent(action)
                    .setContentTitle("불러오는중")
                    .setTicker("asd")
                    .setContentText("이이이ㅣㅣㅣ이ㅣ이ㅣ이ㅣ잉ㅇ")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .build();
        }

        else
        {
            builder = SetBuilderEmpty(NotificationManager.IMPORTANCE_LOW, EMPTYCHANNEL);
            return builder
                    .setContentIntent(action)
                    .setContentTitle("현재 빈 세탁기가")
                    .setTicker("asd")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .build();
        }
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        SettingDB();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (intent.getAction().contains("start")) {
                    h = new Handler();
                    Log.e("백 그라운드", "자기 소개 하기");
                    r = new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            String QR = pref.getString("QR", "CheerUp");
                            boolean emptyAlarm = pref.getBoolean("EmptyAlarm", true);
                            if(!emptyAlarm)
                            {
                                manager.deleteNotificationChannel(EMPTYCHANNEL);
                                return;
                            }
                            startForeground(110, updateNotificationEmpty());
                            h.postDelayed(this, 1000);
                            return;
                        }
                    };
                    h.post(r);
                } else {
                    h.removeCallbacks(r);
                    stopForeground(true);
                    stopSelf();
                }
            }
        }, 0000);// 2초 정도 딜레이를 준 후 시작
        return Service.START_NOT_STICKY;
    }

    public void OnDestroy() {
        super.onDestroy();
    }

    // 노티피케이션 빌드를 세팅해주는 함수
    private NotificationCompat.Builder SetBuilderEmpty(int importance, String cannel)
    {
        NotificationCompat.Builder settingBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = cannel;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BackChannel", importance);

            channel.setDescription(cannel + " description");
            manager.createNotificationChannel(channel);
            settingBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            settingBuilder = new NotificationCompat.Builder(getApplicationContext());
        }
        return settingBuilder;
    }

    private void SettingDB()
    {
        Log.e("디비 값 갖고옴?", "응");
        for(int i = 0 ; i < WASHERCOUNT; i++)
            DB.add(new DatabaseManager("RaspberryPi" + String.valueOf(i+1)));
    }

    private int UsingCount()
    {
        int count = 0;
        for(int i = 0 ; i < DB.size(); i++)
        {
            if(DB.get(i).GetExcution())
                count++;
        }
        return count;
    }
}
