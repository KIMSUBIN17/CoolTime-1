package com.example.timerapplication;

import android.view.View;

public class SoundData
{
    private String soundTile;
    private int idNum;

    public boolean play;

    public SoundData(String _soundTitle, int _idNum)
    {
        this.soundTile = _soundTitle;
        this.idNum = _idNum;
        this.play = false;
    }

    public String getSoundTitle()
    {
        return this.soundTile;
    }

    public int getIdNum()
    {
        return this.idNum;
    }
}
