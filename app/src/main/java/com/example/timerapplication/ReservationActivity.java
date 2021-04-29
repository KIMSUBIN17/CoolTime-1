package com.example.timerapplication;

/*
세탁실에 있는 세탁기의 남은 시간을 보여주는 클래스
 */

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ReservationActivity extends AppCompatActivity {

    ImageView imageViews[] = new ImageView[6];
    TextView textViews[] = new TextView[6];
    private  boolean[] imgEnable = new boolean[6];

    // 그림 페이드인 아웃 모션

    // 로딩하는 아이콘 및 배경 변수
    private ImageView loadingBackground;
    private ImageView loadingIcon;
    private TextView loadingText;
    private Animation loadingBackgroundAnim;
    private Animation loadingIconAnim;

    private ArrayList<DatabaseManager> db = new ArrayList<DatabaseManager>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoadingSetting();
        setContentView(R.layout.reservation_layout);

        int [] Rid_imageView = { R.id.washerImg01, R.id.washerImg02, R.id.washerImg03,
                R.id.washerImg04, R.id.washerImg05, R.id.washerImg06};

        int [] Rid_textView ={R.id.washerTimeText1, R.id.washerTimeText2,R.id.washerTimeText3,
                R.id.washerTimeText4, R.id.washerTimeText5, R.id.washerTimeText6};

        for(int i = 0 ; i < imgEnable.length; i++)
            imgEnable[i] = false;

        // 로딩 관련 변수 할당
        loadingBackground = findViewById( R.id.loading_background);
        loadingIcon = findViewById(R.id.loading_icon);
        loadingText = findViewById(R.id.loadingText);

        //애니메이션값 불러오기
        loadingBackgroundAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.loading_background_tween);
        loadingBackground.startAnimation(loadingBackgroundAnim);

        loadingIconAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.loading_icon_tween);

        loadingIcon.startAnimation(loadingIconAnim);
        loadingText.startAnimation(loadingBackgroundAnim);

        for(int i = 0 ; i < imgEnable.length; i++)
            imgEnable[i] = false;

        for(int i = 1 ; i < 7; i++)
        {
            String num = Integer.toString(i);
            db.add(new DatabaseManager("Washer" + num));
            //리스트 변수에 값을 추가하려면 add
        }


        Animation textFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_fade_out);
        for(int j=0; j<6; j++){
            imageViews[j] = findViewById(Rid_imageView[j]);
            textViews[j] = findViewById(Rid_textView[j]);
            textViews[j].startAnimation(textFadeOut);
        }
        findViewById(R.id.washerImg01);

        //initView();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SendSever();
                //여기에 딜레이 후 시작할 작업들을 입력
            }
        }, 2000);// 2초 정도 딜레이를 준 후 시작
    }


    public void SendSever () {
        for (int i = 0; i < db.size(); i++) {
                SetState(imageViews[i], textViews[i], db.get(i),i);
                SetTimer(imageViews[i],textViews[i], db.get(i));
        }
    }

    private void SetState(ImageView imgView, final TextView textView, final DatabaseManager db, final int counter)
    {
        final Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        final Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        final Animation textFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_fade_out);

        final TextView placeText = findViewById(R.id.statusText);
        SetImgTime(imgView, db);

        imgView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.GetTimeData();
                if(!db.GetExcution())
                    placeText.setText((counter + 1) +"번 세탁기 쉬는중 :|");
                //버튼 다시 누르면 원래대로
                else{
                    placeText.setText((counter + 1) +"번 세탁기 사용중:)");
                    if(v.equals(imageViews[counter])) {
                        if(!imgEnable[counter])
                        {
                            // 눌렀을 때 숫자가 보이게 하고 ture 로 변경
                            imageViews[counter].startAnimation(fadeOut);
                            textViews[counter].setText(GetTimeText(db.min,db.sec));
                            textViews[counter].startAnimation(fadeIn);
                        }
                        else{
                            // 다시 눌렀을 때 원상태로 복구해주고 false 로 변경
                            imageViews[counter].startAnimation(fadeIn);
                            textViews[counter].startAnimation(textFadeOut);
                        }
                        imgEnable[counter] = !imgEnable[counter];
                    }
                }
            }
        });
    }

    private String GetTimeText(int min, int sec)
    {
        String m,s;
        if(min < 10)
            m = String.format("%02d", min); //시간을 예쁘기만들기위해(ex.2시 3분을 02:03)
        else
            m = String.valueOf(min);

        if(sec < 10)
            s = String.format("%02d", sec);
        else
            s = String.valueOf(sec);
        return m + ":" + s;
    }

    private void SetImgTime(ImageView imgView, DatabaseManager db)
    {
        if (db.min <= 45 && db.min > 30) {
            imgView.setImageResource(R.drawable.cloth_dirty_1);
        } else if (db.min <= 30 && db.min > 15) {
            imgView.setImageResource(R.drawable.cloth_dirty_2);
        } else if (db.min <= 15 && db.min > 0) {
            imgView.setImageResource(R.drawable.cloth_dirty_3);
        } else {
            imgView.setImageResource(R.drawable.cloth_clean);
        }
    }

    private void SetTimer(final ImageView imgView, final TextView tv, final DatabaseManager db)
    {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                db.GetExcution();
                db.GetTimeData();
                SetImgTime(imgView, db);
                tv.setText(GetTimeText(db.min,db.sec));
            }
        };

        Timer timer = new Timer();
        timer.schedule(tt, 000 ,500);
    }


    // 로딩 전용 함수
    long repeatTime = 100;
    long timer = 0;
    int index = 0;
    int textIndex = 0;
    TextView loadingTextView;
    private void LoadingSetting()
    {
        final int[] loadingLogoIndex = {R.drawable.logo_1, R.drawable.logo_2, R.drawable.logo_3, R.drawable.logo_4, R.drawable.logo_5, R.drawable.logo_6};
        loadingIcon = findViewById(R.id.loading_icon);
        //String text =
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                loadingIcon.setImageResource(loadingLogoIndex[index]);
                                index++;
                                index %= loadingLogoIndex.length;
                            }
                        });
                        Thread.sleep(repeatTime);
                    } catch (InterruptedException e) {
                        // ooops
                    }
            }
        }).start();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}