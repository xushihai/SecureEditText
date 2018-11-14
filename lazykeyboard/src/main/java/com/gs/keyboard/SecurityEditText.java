package com.gs.keyboard;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * SecurityEditText
 *
 * @author yidong (onlyloveyd@gmaill.com)
 * @date 2018/6/15 08:29
 */
public class SecurityEditText extends AppCompatEditText {
    public SecurityEditText(Context context) {
        super(context);
        init(context,null,-1);
    }

    public SecurityEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public SecurityEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    SecurityKeyboard securityKeyboard;

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        securityKeyboard = new SecurityKeyboard(this, null);
    }

    public void setSecurityConfigure(SecurityConfigure configure ){
        securityKeyboard = new SecurityKeyboard(this, configure);
    }

    public SecurityKeyboard getSecurityKeyboard() {
        return securityKeyboard;
    }
}
