/**
 * @author: RealTechnology
 * Written during the end of 2022
 */

package org.real.technology.scaling.layout;
import android.annotation.NonNull;
import android.view.View;
import android.os.Build;
import android.util.TypedValue;
import android.content.Context;

public class ScalingTools {
    
    /**
     * The base elevation of this view relative to its parent, in pixels.
     *
     * @return The base depth position of the view, in pixels.
     */
    public static float getElevation(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= 21) return view.getElevation();
        return 0f;
    }
    
    public static void setElevation(@NonNull View view, float elevation){
        if( Build.VERSION.SDK_INT >= 21) view.setElevation(elevation);
    }
    
    public static int getActionBarHeight(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
                                            true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                                                                     context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
    
}
