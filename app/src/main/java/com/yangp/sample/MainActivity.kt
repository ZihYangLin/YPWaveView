package com.yangp.sample

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.yangp.sample.databinding.ActivityMainBinding
import com.yangp.ypwaveview.YPWaveView

interface OnColorClickedListener {
    fun onClick(color: Int)
}

enum class ViewType {
    FRONT, BEHIND, BORDE, TEXT
}

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity(), OnColorClickedListener {
    override fun onClick(color: Int) {
        when (viewType) {
            ViewType.FRONT -> {
                binding.viewFrontWave.setBackgroundColor(color)
                binding.waveView2.setFrontWaveColor(color)
            }
            ViewType.BEHIND -> {
                binding.viewBehindWave.setBackgroundColor(color)
                binding.waveView2.setBehindWaveColor(color)
            }
            ViewType.BORDE -> {
                binding.viewBorde.setBackgroundColor(color)
                binding.waveView2.setBorderColor(color)
            }
            ViewType.TEXT -> {
                binding.viewText.setBackgroundColor(color)
                binding.waveView2.setTextColor(color)
            }
            else -> null
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.viewFrontWave.setBackgroundColor(YPWaveView.DEFAULT_FRONT_WAVE_COLOR)
        binding.viewBehindWave.setBackgroundColor(YPWaveView.DEFAULT_BEHIND_WAVE_COLOR)
        binding.viewBorde.setBackgroundColor(YPWaveView.DEFAULT_BORDER_COLOR)
        binding.viewText.setBackgroundColor(YPWaveView.DEFAULT_TEXT_COLOR)
        colorArray = resources.getIntArray(R.array.rainbow)

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setAnimationSpeed(100 - progress)
            }
        })

        binding.seekbarWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setBorderWidth(progress.toFloat())
            }
        })

        binding.seekbarOffset.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setWaveVector(progress.toFloat())
            }
        })

        binding.seekbarWaveoffset.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setWaveOffset(progress)
            }
        })

        binding.seekbarWaveStrong.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setWaveStrong(progress)
            }
        })
        binding.seekbarSpikes.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setStarSpikes(progress + 3)
            }
        })

        binding.seekbarPadding.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.waveView2.setShapePadding(progress.toFloat())
            }
        })
        binding.switchAnimation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.waveView2.startAnimation()
            } else {
                binding.waveView2.stopAnimation()
            }
        }

        binding.switchHiddenText.setOnCheckedChangeListener { _, isChecked ->
            binding.waveView2.setHideText(isChecked)
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioCircle -> {
                    binding.waveView2.setShape(YPWaveView.Shape.CIRCLE)
                }
                R.id.radioSquare -> {
                    binding.waveView2.setShape(YPWaveView.Shape.SQUARE)
                }
                R.id.radioHeart -> {
                    binding.waveView2.setShape(YPWaveView.Shape.HEART)
                }
                R.id.radioStar -> {
                    binding.waveView2.setShape(YPWaveView.Shape.STAR)
                }
                else -> null
            }
        }

        mValueList.clear()
        mValueAdapter = ArrayAdapter<String>(this, R.layout.text_item, mValueList)
        binding.listView.adapter = mValueAdapter

        /*color picker*/
        val adapter = ColorAdapter(colorArray!!, this)
        recyclerView = layoutInflater.inflate(R.layout.color_picker, null) as RecyclerView
        recyclerView!!.layoutManager =
            GridLayoutManager(
                this,
                3,
                GridLayoutManager.VERTICAL,
                false
            )
        recyclerView!!.adapter = adapter
        colorPicker = AlertDialog.Builder(this)
            .setView(recyclerView)
            .create()
    }

    override fun onResume() {
        super.onResume()
        binding.viewFrontWave.setOnClickListener {
            viewType = ViewType.FRONT
            colorPicker?.show()
        }
        binding.viewBehindWave.setOnClickListener {
            viewType = ViewType.BEHIND
            colorPicker?.show()
        }
        binding.viewBorde.setOnClickListener {
            viewType = ViewType.BORDE
            colorPicker?.show()
        }
        binding.viewText.setOnClickListener {
            viewType = ViewType.TEXT
            colorPicker?.show()
        }

        binding.waveView2.setAnimationSpeed(100 - binding.seekbar.progress)
        binding.waveView2.setBorderWidth(binding.seekbarWidth.progress.toFloat())
        binding.waveView2.setWaveVector(binding.seekbarOffset.progress.toFloat())
        binding.waveView2.setWaveOffset(binding.seekbarWaveoffset.progress)
        binding.waveView2.setWaveStrong(binding.seekbarWaveStrong.progress)
        binding.waveView2.setListener { progress, max ->
            mValueList.add("progress=>$progress, max=>$max")
            mValueAdapter?.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.waveView2.listener = null
        binding.viewFrontWave.setOnClickListener(null)
        binding.viewBehindWave.setOnClickListener(null)
        binding.viewBorde.setOnClickListener(null)
        binding.viewText.setOnClickListener(null)
    }

    fun onRefresh(v: View) {
        //創建水位動畫Set
        val animatorSet = AnimatorSet()
        val animPay = ObjectAnimator.ofInt(
            binding.waveView2, "progress", 0, 476
        )
        animPay.duration = 1500
        animPay.interpolator = DecelerateInterpolator()
        animatorSet.playTogether(animPay)
        animatorSet.start()
    }

    inner class ColorAdapter(
        private val colorArray: IntArray,
        private val listener: OnColorClickedListener
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
