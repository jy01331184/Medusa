package com.bundle2;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by tianyang on 16/8/16.
 */
public class Bundle2TextView extends TextView {


    public Bundle2TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.YELLOW);
        setText(R.string.str);
    }


}
