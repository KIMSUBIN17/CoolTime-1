package com.example.timerapplication;

/*
저장 데이터의 기본값을 설정해주는 클래스
C#에서의 Property라고 보면 편함
 */

import android.app.Application;

public class PrefDefaultValue extends Application {
    private boolean ALARMENABELE;
    private String ALARMTITLE;
    private boolean VIBEENABLE;
    private int BEFOREALARMTIME;
    private boolean EMPTYALARM;
    private String QRCODE;
    private int ALARMTITLEINDEX;

    public PrefDefaultValue()
    {
        ALARMENABELE = true;
        ALARMTITLE = "none";
        VIBEENABLE = true;
        BEFOREALARMTIME = 0;
        EMPTYALARM = false;
        QRCODE = "CheerUp";
        ALARMTITLEINDEX = 0;
    }

    // 전역변수 초기화
    public void onCreate()
    {
        ALARMENABELE = true;
        ALARMTITLE = "none";
        VIBEENABLE = true;
        BEFOREALARMTIME = 0;
        EMPTYALARM = false;
        QRCODE = "CheerUp";
        ALARMTITLEINDEX = 0;
        super.onCreate();
    }

    public boolean getAlarmEnable()
    {
        return ALARMENABELE;
    }

    public String getAlarmTitle()
    {
        return ALARMTITLE;
    }

    public boolean getViveEnable()
    {
        return VIBEENABLE;
    }

    public int getBeforAlarmTime()
    {
        return BEFOREALARMTIME;
    }
    public boolean getEmptyAlarm()
    {
        return EMPTYALARM;
    }

    public String getQRCode()
    {
        return QRCODE;
    }

    public int getAlarmTitleIndex()
    {
        return ALARMTITLEINDEX;
    }
}
