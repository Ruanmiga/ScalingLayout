/**
 * @author: RealTechnology
 * Written during the end of 2022
 */

package org.real.technology.scaling.layout;
import android.content.Context;
import android.util.AttributeSet;
import android.content.res.TypedArray;

public class ScalingLayoutSettings {
    
    private static final float DEFAULT_RADIUS_FACTOR = 1.0f;
    
    private float radiusFactor;
    private int initialWidth;
    private int width;
    private float radius;
    private float elevation;
    private boolean isInitialized = false;

    public ScalingLayoutSettings(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScalingLayout);
        radiusFactor = typedArray.getFloat(R.styleable.ScalingLayout_radius, DEFAULT_RADIUS_FACTOR);
        width = context.getResources().getDisplayMetrics().widthPixels;
        typedArray.recycle();

        if (radiusFactor > DEFAULT_RADIUS_FACTOR) {
            radiusFactor = DEFAULT_RADIUS_FACTOR;
        }
    }

    public void initialize(int width, int height) {
        if (!isInitialized()) {
            isInitialized = true;
            initialWidth = width;
            float radiusLimit = height / 2;
            radius = radiusLimit * radiusFactor;
        }
    }

    public int getInitialWidth() {
        return initialWidth;
    }

    public int getWidth() {
        return width;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getElevation() {
        return elevation;
    }
    
}
