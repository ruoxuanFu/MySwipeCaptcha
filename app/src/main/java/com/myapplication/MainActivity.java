package com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.slidingcaptcha.SlidingCaptchaView;

public class MainActivity extends AppCompatActivity {

    private SlidingCaptchaView mSlidingCaptchaView;
    private SeekBar mSeekBar;
    private Button mChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSlidingCaptchaView = findViewById(R.id.slidingCaptchaView);
        mSeekBar = findViewById(R.id.dragBar);
        mChange = findViewById(R.id.btnChange);

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingCaptchaView.createCaptcha();
                mSeekBar.setEnabled(true);
                mSeekBar.setProgress(0);
            }
        });

        mSlidingCaptchaView.setImageDrawable(getResources().getDrawable(R.drawable.image02));
        mSlidingCaptchaView.setOnCaptchaMatchListener(new SlidingCaptchaView.OnCaptchaMatchListener() {
            @Override
            public void onMatchSuccess() {
                Toast.makeText(MainActivity.this, "＼＼" + "\\" + "\\" + "٩('ω')و//／／", Toast.LENGTH_SHORT).show();
                mSeekBar.setEnabled(false);
            }

            @Override
            public void onMatchFail() {
                Toast.makeText(MainActivity.this, "(╯‵□′)╯︵┻━┻ 走你！", Toast.LENGTH_SHORT).show();
                mSlidingCaptchaView.resetCaptcha();
                mSeekBar.setProgress(0);
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSlidingCaptchaView.setCurrentSwipeValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBar.setMax(mSlidingCaptchaView.getMaxSwipeValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSlidingCaptchaView.matchCaptcha();
            }
        });
    }
}
