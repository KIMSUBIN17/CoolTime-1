package com.example.timerapplication;

/*
세팅 엑티비티에 있는 항목들을 눌렀을 시 설정하는 함수 및 변수

 */

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class SettingsActivity extends Activity {
    public  static Context mContext;
    public PrefDefaultValue defaultValue;
/*
    pref 이름 규칙
    알람 OnOff - > AlarmEnable
    알람 이름 - > AlarmTitle
    진동 OnOff - > VibeEnable
    알람전 알람 - > BeforeTimer
    남은 자리 알람 - > EmptyAlarm

    pref 배열 위치마다 어떤걸 할당받음???
    0 - 알람 enable
    1 - 알람 사운드 이름
    2 - 진동 enable
    3 - 알람 전 알람 enable

    pref 클래스로 빼려 했지만 Activity 가 할당 되야 불러 올 수 있는거 같음.
    그래서 클래스로 안만들고 소스 좀 드럽지만 그냥 막 씀
 */
    private SharedPreferences pref;

    private TextView alarmSettingText;

    private Switch[] enabledSwitch = new Switch[2];

    private LinearLayout soundSelectLayout;

    // 사운드 이름 저장하는 리스트
    private ArrayList<Integer> soundList = new ArrayList<Integer>();
    private int titleListIndex;

    // 사운드 목록 리스트
    private ArrayList<String> titleTexts = new ArrayList<String>();

    // 알람 타이머 스피너
    private Spinner timerSpinner;
    private ArrayList<String> beforeTimer = new ArrayList<String>();

    // 남은 자리 알람 버튼 및 변수
    private boolean emptyPlace;
    private Switch emmptyPlaceSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultValue = new PrefDefaultValue();
        mContext = this;
        setContentView(R.layout.first_setting_layout);
        // 처음 액티비티 요소 설정하는 함수
        SettingActivityObject();
        // 스피너 셋팅
        SettingTimerSpinner();
        // pref 할당
        pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        // 사운드 파일 갖고오는 함수
        ReadSound();
        // 처음 액티비티를 설정 하는 함수
        initialSetting();

        Button QRreset = findViewById(R.id.QRreset);
        QRreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("QR", defaultValue.getQRCode());
                editor.commit();
                Toast.makeText(SettingsActivity.this, "QR코드 초기화", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 리소스 폴더에 있는 사운드 리스트 뽑아내는 함수
    private void ReadSound()
    {
        try {
            loadSoundTitleIndex();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    // 액티비티 요소 로드하는 함수
    private void SettingActivityObject()
    {
        // 알람음 선택한 텍스트뷰
        this.alarmSettingText = findViewById(R.id.alarmSoundTitle);

        // 스위치 저장
        this.enabledSwitch[0] = findViewById(R.id.alarmOnSwitch);
        this.enabledSwitch[1] = findViewById(R.id.vibratorOnSwitch);
        settingSwitchNButton();

        // 버튼 저장
        this.soundSelectLayout = findViewById(R.id.soundSelectLayout);

        // 사운드 고르는 버튼
        this.soundSelectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = listViewSetting();
                startActivity(intent);
            }
        });

        this.emmptyPlaceSwitch = findViewById(R.id.emptyWasherAlarm);
    }

    // 대충 설정창 처음 열었을 때 쓰는 함수
    private void initialSetting()
    {
        boolean alarmEnable = pref.getBoolean("AlarmEnable", defaultValue.getAlarmEnable());
        enabledSwitch[0].setChecked(alarmEnable);
        settingAlarm(alarmEnable);
        loadMenu();
        LoadSpinner();
        LoadEmptyPlace();
    }

    // 저장 되어 있던 값 불러옴
    private void settingAlarm(boolean enable)
    {
        enabledSwitch[1].setEnabled(enable);
        soundSelectLayout.setEnabled(enable);
    }

    // 선택했던 벨소리 불러오는 함수
    private void loadMenu()
    {
        enabledSwitch[1].setChecked(pref.getBoolean("VibeEnable", defaultValue.getViveEnable()));
        // 그 다음에 벨소리 불러오기
        String alarmTitle = pref.getString("AlarmTitle", defaultValue.getAlarmTitle());
        alarmSettingText.setText(alarmTitle);
        alarmSettingText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); //칸에 맞춰서 글씨 크기 조정
    }

    // 스피너를 설정 해 준뒤 설정한 값을 갖고오는 함수
    private void LoadSpinner()
    {
        // 스피너 설정
        int a = pref.getInt("BeforeTimer", defaultValue.getBeforAlarmTime());
        Log.e("불러온 값" , GetTimerString(a));
        for(int i = 0; i < beforeTimer.size(); i++) //스피너는 문자열이 아니라 위치로 저장되기 때문에 저장된 값과 스피너의 위치값의 문자를 비교하여 적용
        {
            Log.e("i값은 : ", Integer.toString(i));
            if(GetTimerString(a).equals(timerSpinner.getItemAtPosition(i)))
            {
                timerSpinner.setSelection(i);
                break;
            }
        }
    }

    // 설정한 알람 소리의 인덱스값을 갖고오는 함수
    protected void loadSoundTitleIndex() throws IllegalAccessException {

        Field[] fields = R.raw.class.getFields();
        for(int i = 0 ; i < 9; i++)
        {
            soundList.add(fields[i].getInt(null));
        }
        //titleListIndex = fields[fields.length-1].getInt(null);
    }

    // 설정한 알람 소리의 타이틀을 갖고오는 함ㅅ
    private void loadSountTitleText() throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.zz_sound_list);
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String buffer = null;
        while((buffer = br.readLine()) != null)
        {
            Log.e("타이틀", buffer);
            titleTexts.add(buffer);
        }
    }

    // 진동의 부울값을 저장해주는 함수
    protected void savePref(boolean vibeEnable)
    {
        SharedPreferences.Editor prefEditer;

        prefEditer = pref.edit();
        prefEditer.putBoolean("VibeEnable", vibeEnable);
        prefEditer.commit();
    }


    protected void saveAlarmPref(boolean alaramEnable)
    {
        SharedPreferences.Editor prefEditer;

        if(alaramEnable)
        {
            StartForground();
        }

        else
        {
            Intent forgroundIntent = new Intent(getApplicationContext(), MainActivity.class);
            stopService(forgroundIntent);
        }
        prefEditer = pref.edit();
        prefEditer.putBoolean("AlarmEnable", alaramEnable);
        prefEditer.commit();
    }

    private void settingSwitchNButton()
    {
        // 첫번째로 알람 onoff 버튼
        enabledSwitch[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingAlarm(isChecked);
                saveAlarmPref(isChecked);
            }
        });

        // 두번째로 벨소리 지정 버튼


        // 세번째로 진동 저장 버튼
        enabledSwitch[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePref(isChecked);
            }
        });
    }
    public Intent listViewSetting()
    {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("SoundTitle", titleTexts);
        intent.putExtra("SoundIndex", soundList);
        return intent;
    }

    public void SetTitleText()
    {
        alarmSettingText.setText(pref.getString("AlarmTitle" , "없음"));
        alarmSettingText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
    }
    // 여기서부터는 스피너 설정 하는 함수들
    private void SettingTimerSpinner()
    {
        beforeTimer.add("없음");
        beforeTimer.add("3분");
        beforeTimer.add("5분");
        beforeTimer.add("10분");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item,beforeTimer);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        //없음,3,5,10분을 눌렀을 때 pref에 저장
        ArrayAdapter adapterMK2 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.time, R.layout.spinner_item);
        adapterMK2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        timerSpinner = (Spinner)findViewById(R.id.beforAlarmSpinner);
        timerSpinner.setAdapter(adapterMK2);
        timerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id)
            {
                String str = (String) timerSpinner.getSelectedItem();
                SaveBeforeTimer(str);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });
    }

    // 몇분 전 알람 시간 저장해주는 함수
    private void SaveBeforeTimer(String time)
    {
        SharedPreferences.Editor prefEditer;
        prefEditer = pref.edit();
        prefEditer.putInt("BeforeTimer", GetTimerInteger(time));
        prefEditer.commit();
    }
    //스피너 값 확인
    private int GetTimerInteger(String time)
    {
        int num = 0;
        for(int i = 0 ; i < beforeTimer.size(); i++)
        {
            if(time.equals(beforeTimer.get(i)))
            {
                switch (i)
                {
                    case 0: num = 0; break;
                    case 1: num = 3; break;
                    case 2: num = 5; break;
                    case 3: num = 10; break;
                }
                Log.e("저장된 값", beforeTimer.get(i));
                break;
            }
        }
        return num;
    }

    private String GetTimerString(int time)
    {
        String value = null;
        switch (time)
        {
            case 0: value = "없음"; break;
            case 3: value = "3분"; break;
            case 5: value = "5분"; break;
            case 10: value = "10분"; break;
            default: value = "에러에러"; break;
        }
        return value;
    }

    // 여기서 부터는 남은자리 알람 설정하는 함수들
    private void LoadEmptyPlace()
    {
        emptyPlace = pref.getBoolean("EmptyAlarm", defaultValue.getEmptyAlarm());
        emmptyPlaceSwitch.setChecked(emptyPlace);

        // 자리 비어있는 알람 onoff
        emmptyPlaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingEmptyData(isChecked);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SettingEmptyData(boolean enable)
    {
        emmptyPlaceSwitch.setChecked(enable);
        SaveEmptyPref(enable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SaveEmptyPref(boolean enable)
    {
        SharedPreferences.Editor prefEditer;

        if(enable)
        {
            StartForground();
        }

        else
        {
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.deleteNotificationChannel("IWantRest");
            Intent forgroundIntent = new Intent(getApplicationContext(), MainActivity.class);
            stopService(forgroundIntent);
        }
        prefEditer = pref.edit();
        prefEditer.putBoolean("EmptyAlarm", enable);
        prefEditer.commit();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    private void StartForground()
    {
        Intent intent = getIntent();
        Intent foregroundIngent;

        if(isServiceRunning("com.example.timerapplication.TimerCountManager"))
        {
            Log.e("메인포그라운드 ", "실행중");
        }
        else
        {
            Log.e("메인포그라운드 ", "안실행중");
            foregroundIngent = new Intent(getApplicationContext(), MainActivity.class);
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
}
