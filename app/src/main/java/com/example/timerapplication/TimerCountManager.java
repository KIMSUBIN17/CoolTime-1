package com.example.timerapplication;

/*
그놈의 진짜 너무 짜증나서 없애버리고 싶지만 내가 만들어서 고슴도치도 지 새끼는 이뻐하니까 지우지 못하고 이 어플에 가장 중요한 부분인
노티피케이션을 실행시키는 클래스
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class TimerCountManager extends Service {
    private PrefDefaultValue defaultValue;
    DatabaseManager databaseManager;
    NotificationManager manager;
    public int num;

    // 한번 실행 되게 하는 변수 추가
    private boolean timerOn = false;
    // 알람 한번 실행
    private boolean alarmOn = false;

    // 핸들러
    private Handler h;
    private Runnable r;

    // pref로 값 받아옴 각각 전역변수로 선언 한 뒤에 밑에 함수에서 한번에 받아옴
    private SharedPreferences pref;
    private boolean alarmEnable;
    private int alarmSoundIndex;
    private boolean vibeEnable;
    private int beforeAlarmTime;
    private boolean emptyAlarm;
    private String QR;


    // 노티피 케이션 주기를 계속해서 바꿔주기 위한 상태값
    private enum State
    {
        TIMER, ALARM, END, ENDLESS, NOTHING, ALL, BEFORE, NOTENABLE, EMPTY
    }
    private State STATE;
    private State BEFORE_STATE;

    private String TIMER_CHANNEL = "JR_channel";
    private String ALARM_CHANNEL = "AlarmChannel";
    private String BEFORE_CHANNEL = "BeforeChannel";

    // 빌더 채널 이름
    private String EMPTYCHANNEL = "IWantRest";

    private ArrayList<DatabaseManager> DB = new ArrayList<>();
    private int WASHERCOUNT = 6;

    private int startTimer = 0;


    NotificationManager emptyManager;
    NotificationCompat.Builder emptyBuilder;


    SoundPool soundPool;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        SettingDB();
        defaultValue = new PrefDefaultValue();
        super.onCreate();
    }

    NotificationCompat.Builder builder;
    private boolean run = true;
    private Notification updateNotification() {
        long[] vibe = {200,200,200};
        num++;
        run = true;
        LoadPrefData();
        Context context = getApplicationContext();
        String info = null;
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent action = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        // Flag indicating that if the described PendingIntent already exists, the current one should be canceled before generating a new one.
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(QR.equals(defaultValue.getQRCode()) || !alarmEnable)
        {
            Log.e("QR코드 디폴트", QR);
            if(emptyAlarm)
            {
                String msg = UsingCount() + "개 남았습니다.";
                builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, EMPTYCHANNEL);
                return builder
                        .setContentIntent(action)
                        .setContentTitle("현재 빈 세탁기가")
                        .setTicker("asd")
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.application_logo)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .build();
            }

            else
            {
                // 여기 할당받은 세탁기가 없는경우
                run = false;
                builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
                STATE = State.NOTHING;
                return builder
                        .setContentIntent(action)
                        .setContentTitle("알람을 종료")
                        .setContentText("합니다.")
                        .setSmallIcon(R.drawable.application_logo)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .build();
            }
        }
        else if(!QR.equals(defaultValue.getQRCode()))
        {
            Log.e("QR코드 안디폴트", QR);
            databaseManager.GetTimeData();
            databaseManager.GetExcution();
            info = GetTimeText(databaseManager.min, databaseManager.sec);
            if (databaseManager.sec + databaseManager.min > 0)
                timerOn = true;
        }

        //소리가 끝나지않는다...
        if(databaseManager.min == beforeAlarmTime && databaseManager.sec == 0)
        {
            if(beforeAlarmTime != 0)
            {
                STATE = State.ALARM;
                builder = SetBuilder(NotificationManager.IMPORTANCE_HIGH, BEFORE_CHANNEL);
                h.removeMessages(0);
                h.removeCallbacks(r);
                OnDestroy();
                if(alarmSoundIndex != -1)
                    soundPool.load(TimerCountManager.this, alarmSoundIndex,1);
                if(pref.getBoolean("VibeEnable", defaultValue.getViveEnable()))
                    builder.setVibrate(vibe);
                return builder
                        .setContentIntent(action)
                        .setContentTitle(beforeAlarmTime + "후에")
                        .setTicker(info)
                        .setContentText("세탁이 완료 됩니다.")
                        .setSmallIcon(R.drawable.application_logo)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .build();
            }
        }

        // 3초동안 데이터 베이스 연결
        if(num < 4)
        {
            builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
            return builder
                    .setContentIntent(action)
                    .setContentTitle("불러오는중")
                    .setTicker(info)
                    .setContentText("이이이잉 불러오는중~~~~")
                    .setSmallIcon(R.drawable.application_logo)
                    .setOngoing(true)
                    .build();
        }

        // 시간 카운터를 준 다음에
        else
        {
            // 시간 그냥 도는거  LOW
            if(QR.equals("CheerUp"))
            {
                // 여기 할당받은 세탁기가 없는경우
                builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
                STATE = State.NOTHING;
                return builder
                        .setContentIntent(action)
                        .setContentTitle("알람을 종료")
                        .setTicker(info)
                        .setContentText("합니다.")
                        .setSmallIcon(R.drawable.application_logo)
                        .setOngoing(true)
                        .build();
            }

            else
            {
                // 세탁기가 이미 종료된 경우
                if(databaseManager.GetExcution())
                {
                    if(databaseManager.min + databaseManager.sec == 0)
                    {
                        Intent intenta = new Intent(context,MainActivity.class);
                        intent.putExtra("stop",true);
                        PendingIntent action2 = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
                        builder = SetBuilder(NotificationManager.IMPORTANCE_HIGH, ALARM_CHANNEL);
                        STATE = State.ALARM;
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("QR", defaultValue.getQRCode());
                        edit.commit();
                        h.removeMessages(0);
                        h.removeCallbacks(r);
                        OnDestroy();
                        Log.e("타임", databaseManager.min + ":" + databaseManager.sec );
                        if(pref.getBoolean("VibeEnable", defaultValue.getViveEnable()))
                            builder.setVibrate(vibe);
                        if(alarmSoundIndex != -1)
                            soundPool.load(TimerCountManager.this, alarmSoundIndex,1);
                        return builder
                                .setContentIntent(action2)
                                .setContentTitle("알람 -ing")
                                //.setOnlyAlertOnce(true)
                                .setTicker(info)
                                .setContentText("종료 하겠습니다.")
                                .setSmallIcon(R.drawable.application_logo)
                                .setOngoing(true)
                                .build();
                    }

                    /*
                    else
                    {
                        // 여기 버튼 추가해서 취소해서 하는 걸로 하기
                        builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
                        return builder
                                .setContentIntent(action)
                                .setContentTitle("세탁기가 끝났습니다.")
                                .setTicker(info)
                                .setContentText("종료 하겠습니다.")
                                .setSmallIcon(R.drawable.application_logo)
                                .setOngoing(true)
                                .build();
                    }

                     */
                    else
                    {
                        builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
                        return builder
                                .setContentIntent(action)
                                .setContentTitle("남은시간")
                                .setTicker(info)
                                .setContentText(info)
                                .setSmallIcon(R.drawable.application_logo)
                                .setOngoing(true)
                                .build();
                    }
                }

                else
                {
                    builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
                    return builder
                            .setContentIntent(action)
                            .setContentTitle("남은시간")
                            .setTicker(info)
                            .setContentText(info)
                            .setSmallIcon(R.drawable.application_logo)
                            .setOngoing(true)
                            .build();
                }
            }
        }
    }


    boolean alarmFirst = true;
    long alarmDelaoy = 7000;
    private boolean isRunning = false;
    private int count = 0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(final Intent intent, int flags, int startId) {
        SetSoundPool();
        OffBeforeChannel();
        LoadPrefData();
        Log.e("QR", QR);
        databaseManager = new DatabaseManager(QR);
        num = 0;
        alarmOn = true;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = SetBuilder(NotificationManager.IMPORTANCE_LOW, TIMER_CHANNEL);
        STATE = State.TIMER;
        SettingDB();


        h = new Handler();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // 핸들러 한번 실행
                if(isRunning)
                {
                    OnDestroy();
                }
                else
                    isRunning = true;

                if (intent.getAction().contains("start")) {
                    r = new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            switch(STATE)
                            {
                                case TIMER:
                                    BEFORE_STATE = STATE;
                                    startForeground(101, updateNotification());
                                    h.postDelayed(this, 1000);
                                    break;
                                case ALARM:
                                    if(!alarmFirst)
                                        alarmDelaoy = 0;
                                    Handler mHandler = new Handler();
                                    mHandler.postDelayed(new Runnable()  {
                                        public void run() {
                                            // 0.5초후
                                            BEFORE_STATE = STATE;
                                            manager.deleteNotificationChannel(TIMER_CHANNEL);
                                            startForeground(101, updateNotification());
                                            alarmFirst = false;
                                            OnDestroy();
                                            h.postDelayed(this, 7000);
                                        }
                                    }, alarmDelaoy);
                                case END:
                                    BEFORE_STATE = STATE;
                                    manager.deleteNotificationChannel(ALARM_CHANNEL);
                                    break;
                                case NOTHING:
                                    BEFORE_STATE = STATE;
                                    manager.deleteNotificationChannel(TIMER_CHANNEL);
                                    return;
                                case ALL:
                                    BEFORE_STATE = STATE;
                                    manager.deleteNotificationChannel(TIMER_CHANNEL);
                                    manager.deleteNotificationChannel(ALARM_CHANNEL);
                                    break;
                                case BEFORE:
                                    STATE = State.TIMER;
                                    BEFORE_STATE = STATE;
                                    startForeground(101, updateNotification());
                                    h.postDelayed(this, 3000);
                                    break;
                                case EMPTY:
                                    h.removeCallbacks(r);
                                    OnDestroy();
                                    break;
                                default: return;
                            }
                            if(!run)
                            {
                                Log.e("asdas","ASDASDASDASDAS");
                                h.removeCallbacks(r);
                                stopForeground(true);
                                stopSelf();
                            }
                            return;
                        }
                    };
                    h.post(r);
                }

                else if(intent.getAction().contains("stop"))
                {
                    Log.e("진행", "stop진행함");
                    stopForeground(true);
                    stopSelf();
                    OnDestroy();
                }

                else {
                    h.removeCallbacks(r);
                    stopForeground(true);
                    stopSelf();
                }
            }
        }, 0000);// 2초 정도 딜레이를 준 후 시작
        if (!alarmOn)
            return Service.START_STICKY;
        else
            return Service.START_NOT_STICKY;
    }



    public void OnDestroy() {
        super.onDestroy();
        h.removeMessages(0);
        stopForeground(true);
        run = false;

        if(emptyAlarm || beforeAlarmTime != 0)
        {
            Intent foregroundIngent = new Intent(getApplicationContext(), TimerCountManager.class);
            foregroundIngent.setAction("start");
            startService(foregroundIngent);
        }
    }

    // 노티피케이션 빌드를 세팅해주는 함수
    private NotificationCompat.Builder SetBuilder(int importance, String cannel)
    {
        NotificationCompat.Builder settingBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = cannel;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel", importance);
            if(cannel.equals(ALARM_CHANNEL) && alarmSoundIndex != -1)
            {
                Log.e("채널 설정", Uri.parse("android.resource://" + getPackageName() + "/" + alarmSoundIndex)+"");
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                //channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + alarmSoundIndex), audioAttributes);
            }
            else if(cannel.equals(BEFORE_CHANNEL))
            {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + GetBeforeSound()), audioAttributes);
            }
            channel.setDescription(cannel + " description");
            manager.createNotificationChannel(channel);
            settingBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            settingBuilder = new NotificationCompat.Builder(getApplicationContext());
        }
        return settingBuilder;
    }

    private void LoadPrefData()
    {
        pref  = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        this.alarmEnable = pref.getBoolean("AlarmEnable", defaultValue.getAlarmEnable());
        this.alarmSoundIndex = pref.getInt("AlarmTitleIndex", defaultValue.getAlarmTitleIndex());
        Log.e("사운드", alarmSoundIndex+"");
        this.vibeEnable = pref.getBoolean("VibeEnable", defaultValue.getViveEnable());
        this.beforeAlarmTime = pref.getInt("BeforeTimer", defaultValue.getBeforAlarmTime());
        this.QR = pref.getString("QR", defaultValue.getQRCode());
        this.emptyAlarm = pref.getBoolean("EmptyAlarm", defaultValue.getEmptyAlarm());
    }

    private String GetTimeText(int min, int sec)
    {
        String m,s;
        if(min < 10)
            m = String.format("%02d", min);
        else
            m = String.valueOf(min);

        if(sec < 10)
            s = String.format("%02d", sec);
        else
            s = String.valueOf(sec);
        return m + ":" + s;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DelChannel(String channel)
    {
        manager.deleteNotificationChannel(channel);
        STATE = State.END;
        OnDestroy();
    }

    private int GetBeforeSound()
    {
        int id = 0;
        switch (beforeAlarmTime)
        {
            case 3: id =R.raw.x_ten_time_alarm; break;
            case 5: id = R.raw.y_five_time_alarm; break;
            case 10: id = R.raw.z_three_time_alarm; break;
        }
        return id;
    }

    private void SettingDB()
    {
        Log.e("진행", "DB 데이터 불러옴");
        DB.clear();
        for(int i = 0 ; i < WASHERCOUNT; i++)
            DB.add(new DatabaseManager("Washer" + (i+1)));
    }

    private int UsingCount()
    {
        int count = 0;
        for(int i = 0 ; i < DB.size(); i++)
        {
            if(DB.get(i).GetExcution())
                count++;
        }
        Log.e("세탁기 갯수", count +"");
        return count;
    }


    private NotificationCompat.Builder SetBuilderEmpty(int importance, String cannel)
    {
        NotificationCompat.Builder settingBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = cannel;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BackChannel", importance);

            channel.setDescription(cannel + " description");
            emptyManager.createNotificationChannel(channel);
            settingBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            settingBuilder = new NotificationCompat.Builder(getApplicationContext());
        }
        return settingBuilder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void OffBeforeChannel()
    {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel(TIMER_CHANNEL);
        manager.deleteNotificationChannel(ALARM_CHANNEL);
        manager.deleteNotificationChannel(BEFORE_CHANNEL);
    }

    private void SetSoundPool()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            this.soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(8).build();
        }
        else {
            this.soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int id, int status) {
                soundPool.play(id, 1f, 1f, 0, 0, 1f);
            }
        });
    }
}
