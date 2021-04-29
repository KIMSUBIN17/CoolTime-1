package com.example.timerapplication;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ListItem
{
    private String titleStr ;

    public ListItem(String text){
        this.titleStr = text;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
}
