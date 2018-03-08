package com.example.user.chowdata;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

/**
 * Created by femi on 07/03/2018.
 */

public class FoodChip implements ChipInterface {

    public FoodChip(String label){
        this.label = label;
    }

    String label;

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    public void setLabel(String label){
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getInfo() {
        return null;
    }
}
