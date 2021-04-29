package com.example.timerapplication;

/*
사운드의 이름들을 리스트에 넣기 위해 어댑터에 붙여서 넣주는 클래스
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import java.util.ArrayList;

public class SoundTitleAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    ArrayList<SoundData> sample;

    private String selectedTitle;

    public SoundTitleAdapter(ArrayList<SoundData> data, String prefTitle) {
        this.sample = data;
        this.selectedTitle = prefTitle;
    }

    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public SoundData getItem(int position) {
        return sample.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            convertView.setTag(position);
        }

        CheckedTextView oTextTitle = convertView.findViewById(R.id.soundTitleRadioBtn);
        oTextTitle.setText(sample.get(position).getSoundTitle());
        if(selectedTitle.equals(sample.get(position).getSoundTitle()))
            oTextTitle.setChecked(true);
        return convertView;
    }
}
