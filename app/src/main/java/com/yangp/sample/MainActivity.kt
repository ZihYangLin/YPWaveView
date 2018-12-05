package com.yangp.sample

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.yangp.ypwaveview.YPWaveView
import kotlinx.android.synthetic.main.activity_main.*

interface OnColorClickedListener {
    fun onClick(color: Int)
}

enum class ViewType {
    FRONT, BEHIND, BORDE, TEXT
}

class MainActivity : AppCompatActivity(), OnColorClickedListener {
    override fun onClick(color: Int) {
        when (viewType) {
            ViewType.FRONT -> {
                viewFrontWave.setBackgroundColor(color)
                waveView2.setFrontWaveColor(color)
            }
            ViewType.BEHIND -> {
                viewBehindWave.setBackgroundColor(color)
                waveView2.setBehindWaveColor(color)
            }
            ViewType.BORDE -> {
                viewBorde.setBackgroundColor(color)
                waveView2.setBorderColor(color)
            }
            ViewType.TEXT -> {
                viewText.setBackgroundColor(color)
                waveView2.setTextColor(color)
            }
        }
        colorPicker?.dismiss()
    }

    private var viewType: ViewType? = null
    private var colorArray: IntArray? = null
    private var recyclerView: RecyclerView? = null
    private var colorPicker: AlertDialog? = null
    private var mValueList = ArrayList<String>()
    private var mValueAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFrontWave.setBackgroundColor(YPWaveView.DEFAULT_FRONT_WAVE_COLOR)
        viewBehindWave.setBackgroundColor(YPWaveView.DEFAULT_BEHIND_WAVE_COLOR)
        viewBorde.setBackgroundColor(YPWaveView.DEFAULT_BORDER_COLOR)
        viewText.setBackgroundColor(YPWaveView.DEFAULT_TEXT_COLOR)
        colorArray = resources.getIntArray(R.array.rainbow)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setAnimationSpeed(100 - progress)
            }
        })

        seekbar_width.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setBorderWidth(progress.toFloat())
            }
        })

        seekbar_offset.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setWaveVector(progress.toFloat())
            }
        })

        seekbar_waveoffset.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setWaveOffset(progress)
            }
        })

        seekbar_waveStrong.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setWaveStrong(progress)
            }
        })
        seekbar_spikes.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setStarSpikes(progress + 3)
            }
        })

        seekbar_padding.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveView2.setShapePadding(progress.toFloat())
            }
        })
        switch_animation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                waveView2.startAnimation()
            } else {
                waveView2.stopAnimation()
            }
        }

        switch_hidden_text.setOnCheckedChangeListener { _, isChecked ->
            waveView2.setHideText(isChecked)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioCircle -> {
                    waveView2.setShape(YPWaveView.Shape.CIRCLE)
                }
                R.id.radioSquare -> {
                    waveView2.setShape(YPWaveView.Shape.SQUARE)
                }
                R.id.radioHeart -> {
                    waveView2.setShape(YPWaveView.Shape.HEART)
                }
                R.id.radioStar -> {
                    waveView2.setShape(YPWaveView.Shape.STAR)
                }
            }
        }

        mValueList.clear()
        mValueAdapter = ArrayAdapter<String>(this, R.layout.text_item, mValueList)
        listView.adapter = mValueAdapter

        /*color picker*/
        val adapter = ColorAdapter(colorArray!!, this)
        recyclerView = layoutInflater.inflate(R.layout.color_picker, null) as RecyclerView
        recyclerView!!.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recyclerView!!.adapter = adapter
        colorPicker = AlertDialog.Builder(this)
                .setView(recyclerView)
                .create()
    }

    override fun onResume() {
        super.onResume()
        viewFrontWave.setOnClickListener {
            viewType = ViewType.FRONT
            colorPicker?.show()
        }
        viewBehindWave.setOnClickListener {
            viewType = ViewType.BEHIND
            colorPicker?.show()
        }
        viewBorde.setOnClickListener {
            viewType = ViewType.BORDE
            colorPicker?.show()
        }
        viewText.setOnClickListener {
            viewType = ViewType.TEXT
            colorPicker?.show()
        }

        waveView2.setAnimationSpeed(100 - seekbar.progress)
        waveView2.setBorderWidth(seekbar_width.progress.toFloat())
        waveView2.setWaveVector(seekbar_offset.progress.toFloat())
        waveView2.setWaveOffset(seekbar_waveoffset.progress)
        waveView2.setWaveStrong(seekbar_waveStrong.progress)
        waveView2.setListener { progress, max ->
            mValueList.add("progress=>$progress, max=>$max")
            mValueAdapter?.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        waveView2.listener = null
        viewFrontWave.setOnClickListener(null)
        viewBehindWave.setOnClickListener(null)
        viewBorde.setOnClickListener(null)
        viewText.setOnClickListener(null)
    }

    fun onRefresh(v: View) {
        //創建水位動畫Set
        val animatorSet = AnimatorSet()
        val animPay = ObjectAnimator.ofInt(
                waveView2, "progress", 0, 476)
        animPay.duration = 1500
        animPay.interpolator = DecelerateInterpolator()
        animatorSet.playTogether(animPay)
        animatorSet.start()
    }

    inner class ColorAdapter(private val colorArray: IntArray, private val listener: OnColorClickedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView!!.setBackgroundColor(colorArray[position])
            holder.itemView!!.setOnClickListener { listener.onClick(colorArray[position]) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = View(parent.context)
            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)
            view.layoutParams = lp
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int = colorArray.size
    }
}
