package com.grantmaxson.easycal;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;

public class CustomEditText extends EditText
{
    private OnEditTextImeBackListener mOnImeBack;

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
        {
            if (mOnImeBack != null)
            {
                mOnImeBack.onImeBack(this);
            }
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(OnEditTextImeBackListener listener)
    {
        mOnImeBack = listener;
    }

    public interface OnEditTextImeBackListener
    {
        void onImeBack(CustomEditText ctrl);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_MOVE)
        {
            return true;
        }
        else
        {
            return super.onTouchEvent(event);
        }
    }
}
