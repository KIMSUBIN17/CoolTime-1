package com.example.timerapplication;
/*
알람 리스트를 커스텀한 액티비티에 한개씩 넣는 클래스

 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ListActivity extends AppCompatActivity {
    private ArrayList<ListItem> listItems = new ArrayList<>();
    private ArrayList<SoundData> soundData = new ArrayList<SoundData>();

    // pref 저장하는 변수
    private SharedPreferences pref;

    // 사운드 풀링 해주는 변수
    private SoundPool soundPool;

    private CheckedTextView checkedTV;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound_list_view);
        this.pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        SetSoundData();
        SetSoundPool();
        SettingAdapter();
    }

    private void SettingAdapter()
    {
        final ListView listView = (ListView)findViewById(R.id.listView);

        this.pref = getSharedPreferences("AlarmSetting", MODE_PRIVATE);
        String selected = pref.getString("AlarmTitle", "없음");
        SoundTitleAdapter soundAdapter = new SoundTitleAdapter(soundData, selected);
        listView.setAdapter(soundAdapter);

        Log.e("부", listView.getCount() + "");
        ListAdapter la = listView.getAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                //클릭한 아이템의 리스트 값을 가져옴

                //체크박스 전체 false로 바꿔주기
                for(int i = 0 ; i < listView.getChildCount(); i++)
                {
                    CheckedTextView tv = ((View)listView.getChildAt(i)).findViewById(R.id.soundTitleRadioBtn);
                    tv.setChecked(false);
                }

                //체크박스 선택한것만 true로 바꿔주기
                String selected_item = ((SoundData)adapterView.getItemAtPosition(position)).getSoundTitle();
                CheckedTextView tv = view.findViewById(R.id.soundTitleRadioBtn);
                tv.setChecked(true);
                SoundData selectData = returnData(selected_item);

                if(selectData.play)
                {
                    // 실행 한번 했을 때 저장하기
                    if(selectData.getIdNum() != 1)
                        soundPool.stop(selectData.getIdNum());
                    // 액티비티 닫히고
                    // 설정 창에 있는 텍스트 변경하기
                    //추가할 것:창이 바뀌면 소리 꺼지기
                    finish();
                }

                else if (!selectData.play)
                {
                    Log.e("클릭 했음?? ", String.valueOf(selectData.getIdNum()));
                    // 처음 눌렀을 때 다른 것들 다 false 로 만들고 플레이 하기
                    SetDataBool(); // 다른것들 다 false;
                    selectData.play = true;
                    SaveSoundTitle(selectData.getSoundTitle());
                    SaveSoundIndex(selectData.getIdNum());
                    ((SettingsActivity)SettingsActivity.mContext).SetTitleText(); //알림설정에 나오는 텍스트 변환(없음->나랑 빨래 가지러 갈래)
                    if(selectData.getIdNum() != -1) //없음이 아니면 실행
                        soundPool.load(ListActivity.this, selectData.getIdNum(),1);
                }
            }
        });
    }

    protected void onPause() {
        if(pref.getInt("AlarmTitleIndex", -1) != -1)
            soundPool.stop(pref.getInt("AlarmTitleIndex", -1));
        super.onPause();
    }

    private void SetDataBool()
    {
        for(int i = 0 ; i < soundData.size(); i++)
            soundData.get(i).play = false;
    }

    private void SetSoundData()
    {
        Intent intent = getIntent();
        ArrayList<String> soundTitle = intent.getStringArrayListExtra("SoundTitle");
        ArrayList<Integer> soundIndex = intent.getIntegerArrayListExtra("SoundIndex");

        Log.e("갖고왔나", Integer.toString(soundTitle.size()));

        this.soundData.add(new SoundData("없음", -1));
        for(int i = 0 ; i < soundTitle.size(); i++)
            this.soundData.add(new SoundData(soundTitle.get(i), soundIndex.get(i)));
    }

    private void SetSoundPool()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(8).build();
        }
        else {
            soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int id, int status) {
                soundPool.play(id, 1f, 1f, 0, 0, 1f);
            }
        });
    }

    private SoundData returnData(String title)
    {
        SoundData selectData = null;
        for(int i = 0 ; i < soundData.size(); i++)
        {
            if(soundData.get(i).getSoundTitle() == title)
            {
                selectData = soundData.get(i);
                break;
            }
        }
        if(soundData == null)
            return null;
        else
            return selectData;
    }

    private void SaveSoundTitle(String title)
    {
        SharedPreferences.Editor prefEditer;

        prefEditer = pref.edit();
        prefEditer.putString("AlarmTitle", title);
        prefEditer.commit();
    }

    private void SaveSoundIndex(int index)
    {
        SharedPreferences.Editor prefEditer;

        prefEditer = pref.edit();
        prefEditer.putInt("AlarmTitleIndex", index);
        prefEditer.commit();
    }
}
