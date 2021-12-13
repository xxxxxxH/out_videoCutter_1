package net.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.basicmodel.R
import net.entity.VideoClass
import net.interfaces.AdapterItemTypeCallback
import java.util.*

class GridVideoAdapter(
    private val context: Context,
    videoClassArrayList: ArrayList<VideoClass>,
    adapterItemTypeCallback: AdapterItemTypeCallback
) : RecyclerView.Adapter<GridVideoAdapter.AdapterViewHolder>() {
    private val videoClassArrayList: ArrayList<VideoClass>
    private val adapterItemTypeCallback: AdapterItemTypeCallback

    inner class AdapterViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        var rlMain: RelativeLayout
        var imgThumb: AppCompatImageView
        var imgPlay: AppCompatImageView
        var tvDuration: AppCompatTextView
        override fun onClick(view: View) {
            val id = view.id
            if (id == R.id.rlMainClAllVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(0, view.tag as Int)
            } else if (id == R.id.imgThumbClAllVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(
                    0,
                    view.getTag(R.string.adapter_key) as Int
                )
            } else if (id == R.id.imgPlayClAllVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(0, view.tag as Int)
            }
        }

        init {
            rlMain = view.findViewById(R.id.rlMainClAllVideo)
            imgThumb = view.findViewById(R.id.imgThumbClAllVideo)
            imgPlay = view.findViewById(R.id.imgPlayClAllVideo)
            tvDuration = view.findViewById(R.id.tvDurationClAllVideo)
            rlMain.setOnClickListener(this)
            imgThumb.setOnClickListener(this)
            imgPlay.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_video_grid, parent, false)
        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        try {
            val aClass: VideoClass = videoClassArrayList[position]
            Glide.with(context)
                .load(aClass.ThumbBitmap)
                .into(holder.imgThumb)
            val duration: String = aClass.DisplayDuration
            if (duration != null && !duration.isEmpty()) {
                holder.tvDuration.text = duration
            } else {
                val defDuration = "00:00:00"
                holder.tvDuration.text = defDuration
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.rlMain.tag = position
        holder.imgThumb.setTag(R.string.adapter_key, position)
        holder.imgPlay.tag = position
    }

    override fun getItemCount(): Int {
        return videoClassArrayList.size
    }

    init {
        this.videoClassArrayList = videoClassArrayList
        this.adapterItemTypeCallback = adapterItemTypeCallback
    }
}