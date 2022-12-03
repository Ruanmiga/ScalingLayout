/**
 * @author: RealTechnology
 * Written during the end of 2022
 */

package org.real.technology.scaling.layout;
import android.widget.FrameLayout;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint;
import android.animation.ValueAnimator;
import android.annotation.NonNull;
import android.content.Context;
import android.annotation.Nullable;
import android.util.AttributeSet;
import android.annotation.SuppressLint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.view.ViewGroup;
import android.os.Build;
import android.graphics.Canvas;
import android.annotation.TargetApi;
import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.view.ViewOutlineProvider;
import android.view.View;
import android.graphics.Outline;
import android.support.annotation.RequiresApi;

public class ScalingLayout extends FrameLayout{
    
    /**
     * Settings
     */
    ScalingLayoutSettings settings;

    /**
     * Current radius
     */
    private float currentRadius;

    /**
     * Width is dependent value. It depends on
     * radius. If radius gets updated,
     * layout width will be updated according to this change.
     */
    private int currentWidth;

    /**
     * If layout has margins, margin has to be change
     * according to radius.
     */
    private float[] maxMargins;
    private float[] currentMargins;

    /**
     * State for layout.
     */
    private State state;

    /**
     * Values to draw rounded on layout
     */
    private Path path;
    private Path outlinePath;
    private RectF rectF;
    private Paint maskPaint;

    /**
     * Animator to expand and collapse
     */
    private ValueAnimator valueAnimator;

    /**
     * Listener to notify observer about
     * progress and collapse/expand
     */
    private ScalingLayoutObserver scalingLayoutListener;

    /**
     * CustomOutline for elevation shadows
     */
    @Nullable
    private ScalingOutlineProvider viewOutline;


    public ScalingLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ScalingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScalingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
    public ScalingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Initialize layout
     *
     * @param context
     * @param attributeSet
     */
    public void init(Context context, AttributeSet attributeSet) {
        settings = new ScalingLayoutSettings(context, attributeSet);
        settings.setElevation(ScalingTools.getElevation(this));
        state = State.COLLAPSED;

        path = new Path();
        outlinePath = new Path();
        rectF = new RectF(0, 0, 0, 0);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            final StateListAnimator animator = AnimatorInflater.loadStateListAnimator(context,R.animator.sl_animator);
            setStateListAnimator(animator);
        }
        
        valueAnimator = ValueAnimator.ofFloat(0, 0);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    setRadius((float) valueAnimator.getAnimatedValue());
                }
            });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (maxMargins == null) {
            maxMargins = new float[4];
            currentMargins = new float[4];
            ViewGroup.MarginLayoutParams marginLayoutParams = ((ViewGroup.MarginLayoutParams) getLayoutParams());
            currentMargins[0] = maxMargins[0] = marginLayoutParams.leftMargin;
            currentMargins[1] = maxMargins[1] = marginLayoutParams.topMargin;
            currentMargins[2] = maxMargins[2] = marginLayoutParams.rightMargin;
            currentMargins[3] = maxMargins[3] = marginLayoutParams.bottomMargin;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!settings.isInitialized()) {
            settings.initialize(w, h);
            currentWidth = w;
            currentRadius = settings.getRadius();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) viewOutline = new ScalingOutlineProvider(w, h, currentRadius);
                
        }

        rectF.set(0, 0, w, h);
        updateViewOutline(h, currentWidth, currentRadius);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        path.reset();
        path.addRoundRect(rectF, currentRadius, currentRadius, Path.Direction.CW);
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    /**
     * Provides current {@link ScalingLayoutSettings}
     *
     * @return
     */
    public ScalingLayoutSettings getSettings() {
        return settings;
    }

    /**
     * get current state of layout
     *
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Expand layout to screen
     */
    public void expand() {
        valueAnimator.setFloatValues(settings.getRadius(), 0);
        valueAnimator.start();
    }

    /**
     * Collapse layout to initial position
     */
    public void collapse() {
        valueAnimator.setFloatValues(0, settings.getRadius());
        valueAnimator.start();
    }

    /**
     * This method takes a progress parameter value
     * between 0.0f and 1.0f. And apply this
     * progress value to layout.
     *
     * @param progress
     */
    public void setProgress(float progress) {
        if (progress > 1.0f || progress < 0.0f) return;
        setRadius(settings.getRadius() - (settings.getRadius() * progress));
    }

    public void setObserver(ScalingLayoutObserver observer) {
        this.scalingLayoutListener = observer;
    }

    /**
     * Updates view outline borders and radius
     *
     * @param height
     * @param width
     * @param radius
     */
  private void updateViewOutline(int height, int width, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ScalingTools.getElevation(this) > 0f) {
            try {
                viewOutline.setHeight(height);
                viewOutline.setWidth(width);
                viewOutline.setRadius(radius);
                setOutlineProvider(viewOutline);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set radius will update layout radius
     * Also layouts margins and width depend on
     * radius. So If you update radius, your layout's width
     * and margins will be updated.
     *
     * @param radius
     */
    private void setRadius(float radius) {
        if (radius < 0) return;
        
        updateCurrentRadius(radius);
        updateCurrentWidth(currentRadius);
        updateCurrentMargins(currentRadius);
        updateState(currentRadius);
        updateCurrentElevation();

        getLayoutParams().width = currentWidth;
        ((ViewGroup.MarginLayoutParams) getLayoutParams())
            .setMargins((int) currentMargins[0],
                        (int) currentMargins[1],
                        (int) currentMargins[2],
                        (int) currentMargins[3]);
        requestLayout();
    }

    /**
     * Update current radius
     *
     * @param radius
     */
    private void updateCurrentRadius(float radius) {
        currentRadius = radius < settings.getRadius() ? radius : settings.getRadius();
    }

    /**
     * Update layout width with given radius value
     *
     * @param currentRadius
     */
    private void updateCurrentWidth(float currentRadius) {
        int diffPixel = settings.getWidth() - settings.getInitialWidth();
        float calculatedWidth = (diffPixel - (currentRadius * diffPixel / settings.getRadius())) + settings.getInitialWidth();
        if (calculatedWidth > settings.getWidth()) currentWidth = settings.getWidth();
        else if (calculatedWidth < settings.getInitialWidth()) currentWidth = settings.getInitialWidth();
        else currentWidth = (int) calculatedWidth;
        
    }

    /**
     * Update layout margins with given radius value
     *
     * @param currentRadius
     */
    private void updateCurrentMargins(float currentRadius) {
        currentMargins[0] = maxMargins[0] * currentRadius / settings.getRadius();
        currentMargins[1] = maxMargins[1] * currentRadius / settings.getRadius();
        currentMargins[2] = maxMargins[2] * currentRadius / settings.getRadius();
        currentMargins[3] = maxMargins[3] * currentRadius / settings.getRadius();
    }

    /**
     * Updates layout state
     *
     * @param currentRadius
     */
    private void updateState(float currentRadius) {
        if (currentRadius == 0) state = State.EXPANDED;
        else if (currentRadius == settings.getRadius()) state = State.COLLAPSED;
        else state = State.PROGRESSED;
        
        notifyListener();
    }

   private void updateCurrentElevation() {
        ScalingTools.setElevation(this, settings.getElevation());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ScalingTools.getElevation(this) > 0f) {
            try {
                setOutlineProvider(getOutlineProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Notify observers about change
     */
    private void notifyListener() {
        if (scalingLayoutListener != null) {
            if (state == State.COLLAPSED) scalingLayoutListener.onCollapsed();
            else if (state == State.EXPANDED) scalingLayoutListener.onExpanded();
            else scalingLayoutListener.onProgress(currentRadius / settings.getRadius());
            
            scalingLayoutListener.onStateChange(state);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ViewOutlineProvider getOutlineProvider() {
        return new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setConvexPath(path);
            }
        };
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ScalingOutlineProvider extends ViewOutlineProvider{
        private int width;
        private int height;
        private float radius;

        ScalingOutlineProvider(int width, int height, float radius) {
            this.width = width;
            this.height = height;
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, width, height, radius);
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }
    }
}
