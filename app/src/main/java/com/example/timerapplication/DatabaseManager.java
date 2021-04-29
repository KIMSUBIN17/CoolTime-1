package com.example.timerapplication;

/*
파이어 베이스에 접속 해서 값을 갖고오는 클래스
생성자에 데이터베이스 이름을 넣으면 그 이름에 맞는 데이터를 갖고옴
 */

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;


public class DatabaseManager extends Application
{
    public String dbName;
    // 파이어베이스 변수
    public FirebaseFirestore mFirestore;

    public PrefDefaultValue defaultValue = new PrefDefaultValue();

    // 데이터 베이스 안에 값 저장
    public int min, sec;

    public boolean excution;

    // 이 생성자가 진짜 생성자 이름만 받아오고 나머지는 서버에 접속해서 받아오게 하는 생성자
    public DatabaseManager(String _dbName)
    {
        this.dbName = _dbName;
        this.mFirestore = FirebaseFirestore.getInstance();
        if(_dbName.equals(defaultValue.getQRCode()))
        {
            return;
        }
        GetTimeData();
        GetExcution();
    }

    /*
    while 안에넣거나 if 문 에 넣은다음에 이거 다음에 알랴드림 직접 짠다음에 하
     */
    public boolean GetData()
    {
        GetTimeData();
        Log.i("Fiebase GetData min" , String.valueOf(min));
        if( this.min == 0 && this.sec == 0)
            return SetDefaultData();
        else
            return true;
    }

    private boolean SetDefaultData()
    {
        Log.e("min", String.valueOf(min));
        // 여기 디폴트 데이터 넣기 그건 좀있다 물어보는걸로
        return false;
    }

    public void GetTimeData()
    {
        Log.e("이름", dbName);
        mFirestore.collection("Home").document(dbName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            Task<DocumentSnapshot> task;

            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                server(task);
            }
            public void server(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String min_text = documentSnapshot.get("min").toString();
                    String sec_text = documentSnapshot.get("sec").toString();
                    setTime(min_text, sec_text);
                }

                else
                {
                    Log.d("Fire_log", "Error : " + task.getException().getMessage());
                }
            }
        });
    }

    public void setTime(String minT, String secT)
    {
        this.min = Integer.parseInt(minT); //this를 쓰는 이유: 이 클래스의 값을 확실하게 바꿔주는것(ex.주소를 바꿈)
        this.sec = Integer.parseInt(secT);
    }

    public boolean GetExcution()
    {
        mFirestore.collection("Home").document(dbName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                server(task);
            }
            public void server(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    excution = documentSnapshot.getBoolean("device");
                }

                else
                {
                    Log.d("Fire_log", "Error : " + task.getException().getMessage());
                }
            }
        });
        return this.excution;
    }

    // 처음 세탁기 이름을 저장하는 함수
    public void SetWasherName(String name)
    {
        SharedPreferences nameSetting = getSharedPreferences("WasherName", MODE_PRIVATE);
        SharedPreferences.Editor nameEditor = nameSetting.edit();
        nameEditor.putString("WasherName", name);
        this.dbName = name;
        nameEditor.commit();
    }

    // 세탁기 이름을 가져오는 함수
    public String GetWasherName() {
        SharedPreferences nameSetting = getSharedPreferences("WasherName", MODE_PRIVATE);
        this.dbName = nameSetting.getString("WasherName", null);
        return this.dbName;
    }
}
