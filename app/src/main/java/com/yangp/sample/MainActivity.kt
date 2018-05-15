package com.yangp.sample

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFrontWave.setBackgroundColor(YPWaveView.DEFAULT_FRONT_WAVE_COLOR)
        viewBehindWave.setBackgroundColor(YPWaveView.DEFAULT_BEHIND_WAVE_COLOR)
        viewBorde.setBackgroundColor(YPWaveView.DEFAULT_BORDER_COLOR)
        viewText.setBackgroundColor(YPWaveView.DEFAULT_TEXT_COLOR)
        colorArray = resources.getIntArray(R.array.rainbow)
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
