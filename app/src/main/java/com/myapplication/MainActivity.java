package com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.slidingcaptcha.SlidingCaptchaView;

public class MainActivity extends AppCompatActivity {

    private SlidingCaptchaView withSeeKBar;
    private SeekBar mSeekBar;
    private Button mChange1;
    private SlidingCaptchaView withFinger;
    private Button mChange2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        withSeeKBar = findViewById(R.id.withSeeKBar);
        mSeekBar = findViewById(R.id.dragBar);
        mChange1 = findViewById(R.id.btnChange1);
        withFinger = findViewById(R.id.withFinger);
        mChange2 = findViewById(R.id.btnChange2);

        mChange1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withSeeKBar.createCaptcha();
                mSeekBar.setEnabled(true);
                mSeekBar.setProgress(0);
            }
        });

        mChange2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withFinger.createCaptcha();
            }
        });

        withSeeKBar.setImageDrawable(getResources().getDrawable(R.drawable.image01));
        withSeeKBar.setOnCaptchaMatchListener(new SlidingCaptchaView.OnCaptchaMatchListener() {
            @Override
            public void onMatchSuccess() {
                Toast.makeText(MainActivity.this, "＼＼" + "\\" + "\\" + "٩('ω')و//／／", Toast.LENGTH_SHORT).show();
                mSeekBar.setEnabled(false);
            }

            @Override
            public void onMatchFail() {
                Toast.makeText(MainActivity.this, "(╯‵□′)╯︵┻━┻ 走你！", Toast.LENGTH_SHORT).show();
                withSeeKBar.resetCaptcha();
                mSeekBar.setProgress(0);
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                withSeeKBar.setCurrentSwipeValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBar.setMax(withSeeKBar.getMaxSwipeValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                withSeeKBar.matchCaptcha();
            }
        });

        withFinger.setImageDrawable(getResources().getDrawable(R.drawable.image02));
        withFinger.setSliderMode(SlidingCaptchaView.WITH_FINGER);
        withFinger.setOnCaptchaMatchListener(new SlidingCaptchaView.OnCaptchaMatchListener() {
            @Override
            public void onMatchSuccess() {
                Toast.makeText(MainActivity.this, "(￣▽￣)~*", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMatchFail() {
                Toast.makeText(MainActivity.this, " (*｀皿´*)ﾉ ", Toast.LENGTH_SHORT).show();
                withFinger.resetCaptcha();
            }
        });
    }
}
