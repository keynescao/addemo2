package com.kc.supcattle;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements OnClickListener{

    // 三个tab布局
    private LinearLayout [] layoutTab = new LinearLayout[5];
    private Fragment [] tabFrgment = new Fragment[5];
    private Fragment currentFragment;
    private int currentSelected = -1;

    // 底部标签图片
    private ImageView []imgs = new ImageView[5];
    // 底部标签的文本
    private TextView []tabtxt = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initTab();
        
        
    }


    private void initUI() {

        for(int i=0;i<5;i++){

            Log.d("ID","=======================" +getResources().getIdentifier("tab"+ i,"id",getPackageName()));

            layoutTab[i] = (LinearLayout)findViewById(getResources().getIdentifier("tab"+ i,"id",getPackageName()));
            layoutTab[i].setOnClickListener(this);

            imgs[i] = (ImageView) findViewById(getResources().getIdentifier("tab"+ i+"_img","id",getPackageName()));

            tabtxt[i] = (TextView)findViewById(getResources().getIdentifier("tab"+ i+"_txt","id",getPackageName()));

        }

        tabFrgment[0] = new HomeFragment();
        tabFrgment[1] = new QueryFragment();
        tabFrgment[2] = new DownFragment();
        tabFrgment[3] = new MsgFragment();
        tabFrgment[4] = new PersonFragment();
    }

    /**
     * 初始化底部标签
     */
    private void initTab() {
        changeLayout(1);
    }


    @Override
    public void onClick(View v) {
        int index = 0;
        switch (v.getId()) {
            case R.id.tab0: // 知道
                index = 0;
                break;
            case R.id.tab1: // 我想知道
                index = 1;
                break;
            case R.id.tab2: // 我的
                index = 2;
                break;
            case R.id.tab3: // 我的
                index = 3;
                break;
            case R.id.tab4: // 我的
                index = 4;
                break;
            default:
                break;
        }
        changeLayout(index);
    }


   /* *//**
     * 点击事件
     */
    private void changeLayout(int idx) {
        Resources res=getResources();
        String packageName = getPackageName();
        imgs[idx].setImageResource(res.getIdentifier("tab"+ idx +"_focus","drawable",packageName));
        tabtxt[idx].setTextColor(ContextCompat.getColor(this,R.color.gold));
        if(currentFragment != null){
            imgs[currentSelected].setImageResource(res.getIdentifier("tab"+ currentSelected +"_normal","drawable",packageName));
            tabtxt[currentSelected].setTextColor(ContextCompat.getColor(this,R.color.white));
        }
        addOrShowFragment(getSupportFragmentManager().beginTransaction(), tabFrgment[idx]);
        currentSelected = idx;

    }

    /**



    /**
     * 添加或者显示
     *
     * @param transaction
     * @param fragment
     */
    private void addOrShowFragment(FragmentTransaction transaction, Fragment fragment) {

        if (currentFragment == fragment)
            return;

        if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
            if(currentFragment == null){
                transaction.add(R.id.content_layout, fragment).commit();
            }else{
                transaction.hide(currentFragment)
                        .add(R.id.content_layout, fragment).show(fragment).commit();
            }
        } else {
            transaction.hide(currentFragment).show(fragment).commit();
        }

        currentFragment = fragment;
    }
}
