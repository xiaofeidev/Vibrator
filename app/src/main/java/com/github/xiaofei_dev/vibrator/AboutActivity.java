package com.github.xiaofei_dev.vibrator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.xiaofei_dev.vibrator.MainActivity;
import com.github.xiaofei_dev.vibrator.R;
import com.github.xiaofei_dev.vibrator.util.OpenUtil;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setTheme(this);
        setContentView(R.layout.activity_about);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        setActionBar();
        findViewById(R.id.itemOpenSource).setOnClickListener(this);
        findViewById(R.id.itemScoreAndFeedback).setOnClickListener(this);
        findViewById(R.id.itemDonate).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.itemOpenSource:
                String url = getString(R.string.openSourceLink);
                OpenUtil.openLink(view.getContext(), null, url, false);
                break;
            case R.id.itemScoreAndFeedback:
                OpenUtil.openApplicationMarket(getPackageName(), "com.coolapk.market",
                        view.getContext());
                break;
            case R.id.itemDonate:
                String alipay = getString(R.string.alipay);
                ClipboardManager myClipboard =
                        (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);  //实例化剪切板服务
                ClipData myClip = ClipData.newPlainText("donate", alipay);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getApplicationContext(), getString(R.string.donateToast),
                        Toast.LENGTH_LONG).show();
                break;
        }

    }

    private void setActionBar() {
        setTitle(getResources().getString(R.string.about));

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
