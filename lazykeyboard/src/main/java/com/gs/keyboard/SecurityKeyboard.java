package com.gs.keyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gs.utils.DisplayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 安全键盘主类
 *
 * @author yidong (onlyloveyd@gmaol.com)
 * @date 2018/6/22 07:45
 */
public class SecurityKeyboard extends Dialog {

    private KeyboardView keyboardView;

    /**
     * 字母键盘
     */
    private Keyboard mKeyboardLetter;

    /**
     * 符号键盘
     */
    private Keyboard mKeyboardNumber, mKeyboardSymbol;

    /**
     * 是否为数字键盘
     */
    private boolean isNumber = false;

    /**
     * 是否大写
     */
    private boolean isUpper = false;

    private TextView tvSymbol, tvLetter, tvNumber;
    private View mMainView;
    private ArrayList<String> nums_ = new ArrayList<>();


    private RelativeLayout keyboardViewLy;
    private SecurityConfigure configuration;

    private AppCompatEditText appCompatEditText;
    private Context mContext;
    Handler mHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    public SecurityKeyboard(final AppCompatEditText appCompatEditText, SecurityConfigure securityConfigure) {
        super(appCompatEditText.getContext(), R.style.PopupKeybroadWindow);
        if (securityConfigure == null) {
            configuration = new SecurityConfigure();
            int inputType = appCompatEditText.getInputType();
            //自适应EditText的inputType
            switch (inputType) {
                case InputType.TYPE_CLASS_NUMBER:
                case InputType.TYPE_CLASS_PHONE:
                case InputType.TYPE_NUMBER_FLAG_DECIMAL:
                case InputType.TYPE_NUMBER_FLAG_SIGNED:
                case InputType.TYPE_NUMBER_VARIATION_PASSWORD:
                case InputType.TYPE_NUMBER_VARIATION_NORMAL:
                case InputType.TYPE_CLASS_DATETIME:
                    configuration.setDefaultKeyboardType(KeyboardType.NUMBER).setSymbolEnabled(false).setLetterEnabled(false);
                    break;
                default:
                    configuration.setDefaultKeyboardType(KeyboardType.LETTER);
                    break;
            }
        } else {
            configuration = securityConfigure;
        }

        this.appCompatEditText = appCompatEditText;
        mContext = appCompatEditText.getContext();
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainView = inflater.inflate(R.layout.gs_keyboard, null);
        mMainView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = (Gravity.BOTTOM);
        getWindow().setAttributes(lp);
        getWindow().getDecorView().setPadding(0, 0, 0, 0);//默认就算是MATCH_PARENT也会填不满，有padding

        this.setContentView(mMainView);

        getWindow().setWindowAnimations(R.style.PopupKeybroad);
        if (DisplayUtils.dp2px(mContext, 236) > (int) (DisplayUtils
                .getScreenHeight(mContext) * 3.0f / 5.0f)) {
            mKeyboardLetter = new Keyboard(mContext,
                    R.xml.gs_keyboard_english_land);
            mKeyboardNumber = new Keyboard(mContext, R.xml.gs_keyboard_number_land);
            mKeyboardSymbol = new Keyboard(mContext,
                    R.xml.gs_keyboard_symbols_shift_land);
        } else {
            mKeyboardLetter = new Keyboard(mContext, R.xml.gs_keyboard_english);
            mKeyboardNumber = new Keyboard(mContext, R.xml.gs_keyboard_number);
            mKeyboardSymbol = new Keyboard(mContext,
                    R.xml.gs_keyboard_symbols_shift);
        }


        keyboardView = (KeyboardView) mMainView.findViewById(R.id.keyboard_view);
        keyboardViewLy = (RelativeLayout) mMainView.findViewById(R.id.keyboard_view_ly);


        tvSymbol = (TextView) mMainView.findViewById(R.id.tv_symbol);
        tvLetter = (TextView) mMainView.findViewById(R.id.tv_letter);
        tvNumber = (TextView) mMainView.findViewById(R.id.tv_number);

        if (!configuration.isLetterEnabled()) {
            tvLetter.setVisibility(View.GONE);
        }
        if (!configuration.isNumberEnabled()) {
            tvNumber.setVisibility(View.GONE);
        }
        if (!configuration.isSymbolEnabled()) {
            tvSymbol.setVisibility(View.GONE);
        }

        switchKeyboardType(configuration.getDefaultKeyboardType(),
                configuration.getSelectedColor(), configuration.getUnselectedColor());

        initNumbers();
        randomNumbers();
        switch (configuration.getDefaultKeyboardType().getCode()) {
            case 0:
                keyboardView.setKeyboard(mKeyboardLetter);
                break;
            case 1:
                keyboardView.setKeyboard(mKeyboardNumber);
                break;
            case 2:
                keyboardView.setKeyboard(mKeyboardSymbol);
                break;
            default:
                keyboardView.setKeyboard(mKeyboardLetter);
                break;
        }
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
        tvNumber.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                switchKeyboardType(KeyboardType.NUMBER,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                randomNumbers();
                keyboardView.setKeyboard(mKeyboardNumber);
            }
        });
        tvLetter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                switchKeyboardType(KeyboardType.LETTER,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                keyboardView.setKeyboard(mKeyboardLetter);
            }
        });
        tvSymbol.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                switchKeyboardType(KeyboardType.SYMBOL,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                keyboardView.setKeyboard(mKeyboardSymbol);
            }
        });

        appCompatEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //curEditText.setInputType(InputType.TYPE_NULL);
                    //将光标移到文本最后
                    Editable editable = appCompatEditText.getText();
                    Selection.setSelection(editable, editable.length());
                    hideSystemKeyboard(v);

                    showKeyboard(appCompatEditText);
                    requestFocus();
                }
                return true;
            }
        });


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        this.setOutsideTouchable(true);//不要使用这个避免多次点击输入框会多次关闭显示软键盘

        appCompatEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });


    }


    private void requestFocus() {
        boolean focus = appCompatEditText.requestFocus();
        if (!focus) {
            //整个界面刷新还没有刷新完成，此时requestFocus会失败，导致requestFocus无效
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    appCompatEditText.requestFocus();
                }
            }, 50);
        }
    }

    /**
     * @param popupWindow popupWindow 的touch事件传递
     * @param touchModal  true代表拦截，事件不向下一层传递，false表示不拦截，事件向下一层传递
     */
    @SuppressLint("PrivateApi")
    private void setPopupWindowTouchModal(PopupWindow popupWindow,
                                          boolean touchModal) {
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
                    boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideSystemKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initNumbers() {
        nums_.clear();
        nums_.add("48#0");
        nums_.add("49#1");
        nums_.add("50#2");
        nums_.add("51#3");
        nums_.add("52#4");
        nums_.add("53#5");
        nums_.add("54#6");
        nums_.add("55#7");
        nums_.add("56#8");
        nums_.add("57#9");
    }

    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = appCompatEditText.getText();
            int start = appCompatEditText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                // 完成按钮所做的动作
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                // 大小写切换
                changeKey();
                keyboardView.setKeyboard(mKeyboardLetter);

            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                // 数字键盘切换,默认是英文键盘
                if (isNumber) {
                    isNumber = false;
                    keyboardView.setKeyboard(mKeyboardLetter);
                } else {
                    isNumber = true;
                    keyboardView.setKeyboard(mKeyboardNumber);
                }
            } else if (primaryCode == 57419) {
                //左移
                if (start > 0) {
                    appCompatEditText.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) {
                //右移
                if (start < appCompatEditText.length()) {
                    appCompatEditText.setSelection(start + 1);
                }
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = mKeyboardLetter.getKeys();
        if (isUpper) {
            isUpper = false;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.keyboard_shift);
                }
            }
        } else {// 小写切换大写
            isUpper = true;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.keyboard_shift_c);
                }
            }
        }
    }

    /**
     * 键盘数字随机切换
     */
    private void randomNumbers() {
        List<Key> keylist = mKeyboardNumber.getKeys();
        ArrayList<String> temNum = new ArrayList<>(nums_);

        for (Key key : keylist) {
            if (key.label != null && isNumber(key.label.toString())) {
                int number = new Random().nextInt(temNum.size());
                String[] text = temNum.get(number).split("#");
                key.label = text[1];
                key.codes[0] = Integer.valueOf(text[0], 10);
                temNum.remove(number);
            }
        }
    }

    public int dipToPX(Context paramContext, float paramFloat) {
        return (int) TypedValue.applyDimension(1, paramFloat, paramContext.getResources().getDisplayMetrics());
    }

    /**
     * 弹出键盘
     *
     * @param view
     */
    private void showKeyboard(View view) {
        if (isShowing()) {
            return;
        }
        show();
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        this.dismiss();
    }

    private boolean isLetter(String str) {
        String letterStr = mContext.getString(R.string.aToz);
        return letterStr.contains(str.toLowerCase());
    }

    private boolean isNumber(String str) {
        String numStr = mContext.getString(R.string.zeroTonine);
        return numStr.contains(str.toLowerCase());
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 键盘类型切换后文本颜色变化
     *
     * @param keyboardType
     * @param selectedColor
     * @param unSelectedColor
     */
    private void switchKeyboardType(KeyboardType keyboardType, int selectedColor, int unSelectedColor) {
        switch (keyboardType.getCode()) {
            case 0:
                tvLetter.setTextColor(selectedColor);
                tvSymbol.setTextColor(unSelectedColor);
                tvNumber.setTextColor(unSelectedColor);
                break;
            case 1:
                tvNumber.setTextColor(selectedColor);
                tvSymbol.setTextColor(unSelectedColor);
                tvLetter.setTextColor(unSelectedColor);
                break;
            case 2:
                tvSymbol.setTextColor(selectedColor);
                tvLetter.setTextColor(unSelectedColor);
                tvNumber.setTextColor(unSelectedColor);
                break;
            default:
                throw new IllegalArgumentException("不支持的键盘类型");
        }
    }

    /**
     * 获取所有子元素
     *
     * @param parent
     * @return
     */
    private List<View> getAllChildren(View parent) {
        List<View> allChildren = new ArrayList<>();
        if (parent instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) parent;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View child = vp.getChildAt(i);
                allChildren.add(child);
                //再次 调用本身（递归）
                allChildren.addAll(getAllChildren(child));
            }
        }
        return allChildren;
    }
}
