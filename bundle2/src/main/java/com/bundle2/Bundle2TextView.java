package com.bundle2;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by tianyang on 16/8/16.
 */
public class Bundle2TextView extends TextView {


    public Bundle2TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.ic);
        setText(R.string.str);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.Bundle2TextView);
        System.out.println(typedArray.getText(R.styleable.Bundle2TextView_banner));

    }


}
