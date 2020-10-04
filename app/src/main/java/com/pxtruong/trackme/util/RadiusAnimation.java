package com.pxtruong.trackme.util;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.google.android.gms.maps.model.GroundOverlay;

public class RadiusAnimation extends Animation {
    private GroundOverlay groundOverlay;
    private int startingSize = 10;

    public RadiusAnimation(GroundOverlay groundOverlay, int startingSize) {
        this.groundOverlay = groundOverlay;
        this.startingSize = startingSize;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        groundOverlay.setDimensions((startingSize * interpolatedTime));
        groundOverlay.setTransparency(interpolatedTime);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }
}
