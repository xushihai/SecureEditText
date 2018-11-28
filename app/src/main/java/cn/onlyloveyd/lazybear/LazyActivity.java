package cn.onlyloveyd.lazybear;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.gs.keyboard.SecurityKeyboard;

public class LazyActivity extends AppCompatActivity {

    SecurityKeyboard securityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = LayoutInflater.from(this).inflate(R.layout.activity_lazy, null);
        setContentView(view);
//
//        SecurityConfigure configure = new SecurityConfigure()
//                .setDefaultKeyboardType(KeyboardType.LETTER);
//        ((SecurityEditText) findViewById(R.id.login_input_password)).setSecurityConfigure(configure);
    }


}
