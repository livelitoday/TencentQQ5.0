package com.example.tencentqq50;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * SlidingMenu界面开发
 */
public class MainActivity extends AppCompatActivity {
    SlidingMenu mSlidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_main_layout);
        mSlidingMenu = (SlidingMenu) findViewById(R.id.sliding_menu);
        findViewById(R.id.user_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换菜单
                mSlidingMenu.toggleMenu();
            }
        });
    }
}
