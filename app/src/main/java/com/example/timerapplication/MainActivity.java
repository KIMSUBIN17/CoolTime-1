package com.example.timerapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.natasa.progressviews.CircleProgressBar;
import com.natasa.progressviews.utils.OnProgressViewListener;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button timer_btn;
    private Button reser_btn;
    private Button setting_btn;

    public FirebaseFirestore mFirestore;

    // 세이브 데이터 갖고옴
    private SharedPreferences pref;
    private PrefDefaultValue defaultValue;
    private String QR;

    /*
    해야될 것 들
    메인 창에서 QR코드의 내용이 비어있으면
    아 아니네
    상관 없네
    저기 타이머 창에서 QR코드 비어있으면 디폴트값 출력
    노티피케이션 버그 고치기
     */

    // 데이터 베이스 받아오는 끄라스 변수
    private static DatabaseManager databaseManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getBooleanExtra("stop", false))
        {
            StopForground();
            QR = pref.getString("QR", defaultValue.getQRCode()); //
        }

        else
        {
            StartForground();
        }
        setForground();
        Log.e("이름", TimerCountManager.class.getName());
        SettingPref();

        if(QR.equals(defaultValue.getQRCode()))
        {
            // 여기에 아무것도 안 하는거 넣기
        }
        else
            databaseManager = new DatabaseManager(QR);

        // Foreground 실행

        setContentView(R.layout.activity_main);
        mFirestore = FirebaseFirestore.getInstance();
        SettingQRScanButton();
        ObjectSetting();
        settingBubble();
    }

    private void setBubbleTouch(final ImageView b)
    {
        final boolean[] bubbleTouch = {false}; //false는 안눌렀을 때 상황
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bubbleTouch[0])
                {
                    return;
                }

                else
                {
                    b.setAlpha(0); //투명값설정(0이면 완전투명)
                    bubbleTouch[0] = true;

                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable()  {
                        public void run() {
                            // 시간 지난 후 실행할 코딩
                            bubbleTouch[0] = false;
                            b.setAlpha(255);
                        }
                    }, 3000); // 0.5초후
                }
            }
        });
    }

    int index = 0;
    private void settingBubble() //setBubbleTouch()에 하나씩 할당
    {
        final ImageView[] bubble = {findViewById(R.id.bubble1), findViewById(R.id.bubble2), findViewById(R.id.bubble3), findViewById(R.id.bubble4), findViewById(R.id.bubble5)};
        final boolean[] bubbleTouch = {false, false, false, false, false};
        index = 0;
        for(int i = 0 ; i < bubble.length; i++)
        {
            setBubbleTouch(bubble[i]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setForground()
    {
        /*
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel("AlarmChannel");
        manager.deleteNotificationChannel("BeforeChannel");
        manager.deleteNotificationChannel("IWantRest");
        Intent foregroundIngent = new Intent( MainActivity.this, TimerCountManager.class);
        foregroundIngent.setAction("stop");
        startService(foregroundIngent);

         */
    }

    private void SettingPref()
    {
        pref  = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        this.defaultValue = new PrefDefaultValue();
        this.QR = pref.getString("QR", defaultValue.getQRCode()); //key값으로 어떤 액티비티든 불러올수 있음
        //get 갖고오는것
        //set 설정하는것
    }

    public void onStart()
    {
        super.onStart();
        StartForground();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private void ObjectSetting()
    {
        timer_btn = (Button) findViewById(R.id.timer_btn);
        reser_btn = (Button) findViewById(R.id.state_btn);
        setting_btn = (Button) findViewById(R.id.setting_btn);


        //예약버튼 눌렀을 때
        reser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        //설정버튼 눌렀을 때
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 타이머 버튼 눌렀을 때
        timer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    IntentIntegrator qrScan;
    public void SettingQRScanButton()
    {
        Button QRScanBtn = findViewById(R.id.qrcode_btn);
        qrScan = new IntentIntegrator(this);
        QRScanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //scan option
                qrScan.setPrompt("Scanning...");
                //qrScan.setOrientationLocked(false);
                qrScan.initiateScan();
            }
        });
    }

    // QR 코드가 유효한 코드인지 확인하는 함수
    private boolean CheckQRCode(String QR)
    {
        boolean checkResult = true;
        String checkString = "Washer";
        for(int i=0;i<checkString.length();i++)  //출력
        {
            //들어가는 철자가 맞는지 확인. 하지만 똑같지않아도 맞다고 하는경우가 있음 ex.was==washer
            if(QR.charAt(i) != checkString.charAt(i))
            {
                checkResult = false;
                break;
            }
        }

        if(checkString.length() >= QR.length())
            checkResult = false;
        // QR 코드가 쓸 수 있는 코드인지 확인
        return checkResult;
    }

    private String getPlaceNumber(String QR)
    {
        String number = QR.replaceAll("[^0-9]", "");
        return number;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(MainActivity.this, "취소!", Toast.LENGTH_SHORT).show();
            }
            //qrcode가 있으면
            else {
                String QR = result.getContents();
                String QRNumber;
                if(CheckQRCode(QR))
                {
                    setQRCode(QR);
                    QRNumber = getPlaceNumber(QR);
                    Toast.makeText(MainActivity.this, QRNumber+"번 자리 세탁기 입니다.", Toast.LENGTH_SHORT).show();
                    // 여기 QR코드 저장하는 함수 만들어서 넣고
                    // 카운터 해주는 액티비티 실행 시켜 주고
                    Intent intent = new Intent(MainActivity.this, TimerActivity.class);
                    startActivity(intent);
                    // 백그라운드 다시 실행해주기
                    StartForground();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "사용할 수 없는 QR코드 입니다.", Toast.LENGTH_SHORT).show();
                }
                //qrcode 결과가 있으면

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setQRCode(String QR)
    {
        SharedPreferences pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("QR", QR);
        editor.commit();
    }

    private void StartForground()
    {
        Intent foregroundIngent = new Intent(MainActivity.this, TimerCountManager.class);

        if(isServiceRunning("com.example.timerapplication.TimerCountManager"))
        {
            Log.e("메인포그라운드 ", "실행중");
        }
            else
        {
            Log.e("메인포그라운드 ", "안실행중");
            foregroundIngent = new Intent(getApplicationContext(), TimerCountManager.class);
            foregroundIngent.setAction("start");
            startService(foregroundIngent);
            Log.e("진입", "빠져나옴");
        }
    }

    public Boolean isServiceRunning(String serviceName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            System.out.println(runningServiceInfo.service.getClassName());
            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StopForground()
    {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel("IWantRest");
        Intent forgroundIntent = new Intent(getApplicationContext(), MainActivity.class);
        stopService(forgroundIntent);
    }
}






