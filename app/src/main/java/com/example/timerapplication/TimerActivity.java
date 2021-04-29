package com.example.timerapplication;

/*
타이머에 관한 클래스
QR코드로 검색 -> 그 세탁기에 맞는 타이머 재생
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class TimerActivity extends Activity {
    private PrefDefaultValue defaultValue;
    private SharedPreferences pref;

    private DatabaseManager DB;
    private ImageView waterLayoutOne;
    private ImageView holeImg;
    private TextView timerText;
    private float maxNum = 100;
    private float percent = 5; //y축을 5등분

    private EditText x;
    private EditText y;

    private View v;

    private float sizeX, sizeY;

    private String noHaveWahser = "세탁기 없음 ㅡㅡ 빨리 돌리고 QR 찍고 오세요!!!!";

    private TextView timer;
    private TextView currentStateText;
    private TextView currentWasherText;

    // 움짤 전용 변수
    private ImageView waterImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_layout);
        pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);

        defaultValue = new PrefDefaultValue();

        String QR = pref.getString("QR", defaultValue.getQRCode());

        ImgRepeat();
        if(QR.equals(defaultValue.getQRCode())) //default값과 같으면 작동
        {
            TextView titleUp = findViewById(R.id.setting_title2);
            TextView titleDown = findViewById(R.id.setting_title);
            titleUp.setText("지금은 세탁기가 쉬는중~");
            titleDown.setText("QR코드를 찍으면\n타이머가 작동해용:)");
            TextView timeText = findViewById(R.id.timeText000);
            timeText.setText("00:00");
            ImageView redLine = findViewById(R.id.redLine);
            redLine.setAlpha(0);
        }

        else
        {
            DB = new DatabaseManager(QR);

            maxNum = 37 * 60;
            DB.GetTimeData();

            //앱 전체의 해상도를 알아야 물 높낮이의 비율을 정할 수 있음
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            sizeX = displayMetrics.widthPixels;
            sizeY = displayMetrics.heightPixels;

            SetTimer();

            setContentView(R.layout.timer_layout);
            v = getWindow().getDecorView();
        }
        UpdateWaterLevel();
    }

    int count = 0;
    // 움짤 그림 할당 후 반복
    private void ImgRepeat()
    {
        waterImg = findViewById(R.id.waterLevel);
        final int i = 0;
        final int[] water = {R.drawable.water_1, R.drawable.water_2, R.drawable.water_3, R.drawable.water_4, R.drawable.water_5, R.drawable.water_6};
        Log.e("워터 ", water[0] + "");
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(150);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                waterImg.setImageResource(water[count]);
                                count++;
                                count %= water.length; //다시 처음그림으로 돌아가기위해
                            }
                        });
                    } catch (InterruptedException e) {
                        // ooops
                    }
            }
        }).start();
    }

    private void HaveQRCode()
    {
        waterImg = findViewById(R.id.waterLevel);
        timer = findViewById(R.id.timeText000);
        currentStateText = findViewById(R.id.setting_title2);
        currentWasherText = findViewById(R.id.setting_title);
        DB.GetExcution();
        DB.GetTimeData();
        Log.e("DB시간" , DB.min + "" + DB.sec);
        timer.setText(GetTimeText(DB.min, DB.sec));
        currentStateText.setText("세탁이 끝나기까지 남은시간은?");
        currentWasherText.setText(getPlaceNumber(DB.dbName)+ "번 세탁기 사용중...");
    }
    private Thread timeTR;
    private void SetTimer()
    {
        TextView titleUp = findViewById(R.id.setting_title2);
        titleUp.setText("세탁이 끝나기까지 남은시간은?");
        timeTR = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                HaveQRCode();
                                if(!run)
                                {
                                    timeTR.interrupt();
                                    onDestroy();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        // ooops
                    }
            }
        });
        timeTR.start();
    }

    private Handler h;
    private Thread tr;
    private boolean run = true;
    //물높낮이를 시간에 따라서 조절하는 함수
    private void UpdateWaterLevel()
    {
        waterLayoutOne = findViewById(R.id.waterLevel);
        timerText = findViewById(R.id.timeText000);

        final String QR = pref.getString("QR", defaultValue.getQRCode());
        Log.e("물결최대 ", waterLayoutOne.getY()+"");
        Log.e("물결최소 ", waterLayoutOne.getHeight()+"");

        this.tr = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                if(!QR.equals(defaultValue.getQRCode()))
                                {
                                    DB.GetTimeData();
                                    int min = DB.min;
                                    int sec = DB.sec;
                                    timerText.setText(GetTimeText(min,sec));

                                    //
                                    waterLayoutOne.setY((CalWaterLevel(maxNum) - CalWaterLevel(min * 60 + sec)) *1.2f + (sizeY / 6.5f));
                                    if(!run)
                                    {
                                        tr.interrupt();
                                        onDestroy();
                                    }
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        // ooops
                    }
            }
        });
        tr.start();
    }


    private float CalWaterLevel(float val)
    {
        float per = val / maxNum;
        float start = sizeY / percent;
        float result = (start * per);
        return result;
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

    private String getPlaceNumber(String QR)
    {
        String number = QR.replaceAll("[^0-9]", "");
        return number;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        run = false;
        tr.interrupt();
        finish();
    }

    // 로딩 전용 함수
    long repeatTime = 100;
    int index = 0;
    int textIndex = 0;
    TextView loadingTextView;



}
