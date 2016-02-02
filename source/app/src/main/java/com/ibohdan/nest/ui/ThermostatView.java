package com.ibohdan.nest.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.ibohdan.nest.R;

public class ThermostatView extends View {

    private Paint paint;
    private Paint paintTarget;
    private Paint paintCurrent;

    private int width;
    private int height;
    private int left;
    private int top;

    private float tempCurrent = 22;
    private float tempTarget = 32;

    private float tempTargetHigh = MAX_VALUE;
    private float tempTargetLow = MIN_VALUE;

    private static final float MAX_VALUE = 32;
    private static final float MIN_VALUE = 9;
    private static final float SEGMENTS_COUNT = (MAX_VALUE - MIN_VALUE) * 2;

    private boolean showHighLowTemp = false;

    public ThermostatView(Context context) {
        super(context);
        init(null);
    }

    public ThermostatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThermostatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ThermostatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        Resources resources = getResources();
        final float density = resources.getDisplayMetrics().density;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(density * 2);
        paint.setColor(resources.getColor(R.color.thermostat_bars));
        paint.setStrokeCap(Paint.Cap.ROUND);

        paintTarget = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTarget.setColor(resources.getColor(R.color.thermostat_target_heat));
        paintTarget.setStrokeWidth(density * 4);
        paintTarget.setStyle(Paint.Style.FILL);

        paintCurrent = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCurrent.setColor(resources.getColor(R.color.thermostat_current));
        paintCurrent.setStrokeWidth(density * 4);
        paintCurrent.setStyle(Paint.Style.FILL);

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ThermostatView, 0, 0);
            final int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                final int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.ThermostatView_barsColor:
                        paint.setColor(a.getColor(attr, paint.getColor()));
                        break;
                    case R.styleable.ThermostatView_barsWidth:
                        paint.setStrokeWidth(a.getDimension(attr, paint.getStrokeWidth()));
                        break;
                    case R.styleable.ThermostatView_currentColor:
                        paintCurrent.setColor(a.getColor(attr, paintCurrent.getColor()));
                        break;
                    case R.styleable.ThermostatView_targetColor:
                        paintTarget.setColor(a.getColor(attr, paintTarget.getColor()));
                        break;
                }
            }
            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int w = right - left;
        final int h = bottom - top;

        this.left = left + getPaddingLeft();
        this.top = top + getPaddingTop();

        if (w != width || h != height) {
            width = w - getPaddingLeft() - getPaddingRight();
            height = h - getPaddingTop() - getPaddingBottom();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //final int savedColor = paint.getColor();
        if (!isEnabled()) {
            //paint.setAlpha(100);
        }
        float topPadding = height / SEGMENTS_COUNT;
        for (int i = 0; i <= SEGMENTS_COUNT; i++) {
            float startY = top + topPadding * i;
            canvas.drawLine(left, startY, left + width, startY + 2, paint);
        }
        canvas.restore();
        if (!isEnabled()) {
            //paint.setColor(savedColor);
        }

        if (isEnabled()) {
            final int save = canvas.save();

            // current
            {
                int count = (int) (SEGMENTS_COUNT - 2 * (tempCurrent - MIN_VALUE));
                float startY = top + count * topPadding;
                float halfWidth = paintCurrent.getStrokeWidth() / 2;
                canvas.drawRect(left, startY - halfWidth, left + width, startY + halfWidth, paintCurrent);
            }

            if (showHighLowTemp) {
                {  // target high
                    paintTarget.setColor(getResources().getColor(R.color.thermostat_target_heat));
                    int count = (int) (SEGMENTS_COUNT - 2 * (tempTargetHigh - MIN_VALUE));
                    float startY = top + count * topPadding;
                    float halfWidth = paintTarget.getStrokeWidth() / 2;
                    canvas.drawRect(left - getPaddingLeft(), startY - halfWidth, left + width + getPaddingRight(), startY + halfWidth, paintTarget);
                }
                {  // target low
                    paintTarget.setColor(getResources().getColor(R.color.thermostat_target_cool));
                    int count = (int) (SEGMENTS_COUNT - 2 * (tempTargetLow - MIN_VALUE));
                    float startY = top + count * topPadding;
                    float halfWidth = paintTarget.getStrokeWidth() / 2;
                    canvas.drawRect(left - getPaddingLeft(), startY - halfWidth, left + width + getPaddingRight(), startY + halfWidth, paintTarget);
                }
            } else {  // target
                paintTarget.setColor(getResources().getColor(R.color.thermostat_target_heat));
                int count = (int) (SEGMENTS_COUNT - 2 * (tempTarget - MIN_VALUE));
                float startY = top + count * topPadding;
                float halfWidth = paintTarget.getStrokeWidth() / 2;
                canvas.drawRect(left - getPaddingLeft(), startY - halfWidth, left + width + getPaddingRight(), startY + halfWidth, paintTarget);
            }
            canvas.restoreToCount(save);
        }
    }

    public void setColorCurrent(int color) {
        paintCurrent.setColor(color);
        invalidate();
    }

    public void setColorTarget(int color) {
        paintTarget.setColor(color);
        invalidate();
    }

    public void setCurrentTemperature(float tempCurrent) {
        this.tempCurrent = validateTemp(tempCurrent);
        invalidate();
    }

    public void setTargetTemperature(float tempTarget) {
        this.tempTarget = validateTemp(tempTarget);
        invalidate();
    }

    public void setTargetHighTemperature(float tempTargetHigh) {
        this.tempTargetHigh = validateTemp(tempTargetHigh);
        invalidate();
    }

    public void setTargetLowTemperature(float tempTargetLow) {
        this.tempTargetLow = validateTemp(tempTargetLow);
        invalidate();
    }

    public void setShowHighLowTemp(boolean showHighLowTemp) {
        if (this.showHighLowTemp != showHighLowTemp) {
            this.showHighLowTemp = showHighLowTemp;
            invalidate();
        }
    }

    private float validateTemp(float temp) {
        if (temp > MAX_VALUE) {
            return MAX_VALUE;
        }
        if (temp < MIN_VALUE) {
            return MIN_VALUE;
        }
        return temp;
    }
}
