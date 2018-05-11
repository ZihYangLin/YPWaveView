package com.yangp.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yangp.ypwaveview.YPWaveView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFrontWave.setBackgroundColor(YPWaveView.DEFAULT_FRONT_WAVE_COLOR)
        viewBehindWave.setBackgroundColor(YPWaveView.DEFAULT_BEHIND_WAVE_COLOR)
        viewBorde.setBackgroundColor(YPWaveView.DEFAULT_BORDER_COLOR)
        viewText.setBackgroundColor(YPWaveView.DEFAULT_TEXT_COLOR)
    }

}
