package net.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.basicmodel.R
import net.entity.VideoClass
import net.interfaces.AdapterItemTypeCallback
import net.widget.SquareImageView
import java.util.*

class ListVideoAdapter(
    private val context: Context,
    videoClassArrayList: ArrayList<VideoClass>,
    adapterItemTypeCallback: AdapterItemTypeCallback
) :
    RecyclerView.Adapter<ListVideoAdapter.AdapterViewHolder>() {
    private val videoClassArrayList: ArrayList<VideoClass>
    private val adapterItemTypeCallback: AdapterItemTypeCallback

    inner class AdapterViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        var llMain: LinearLayout
        var imgThumb: SquareImageView
        var imgPlay: AppCompatImageView
        var tvDuration: AppCompatTextView
        var tvName: AppCompatTextView
        var tvSize: AppCompatTextView
        var imgEdit: AppCompatImageView
        var imgDelete: AppCompatImageView
        var imgShare: AppCompatImageView
        override fun onClick(view: View) {
            val id = view.id
            if (id == R.id.llMainClVideoList) {
                adapterItemTypeCallback.onItemTypeClickCallback(0, view.tag as Int)
            } else if (id == R.id.imgThumbClMyVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(
                    0,
                    view.getTag(R.string.adapter_key) as Int
                )
            } else if (id == R.id.imgPlayClMyVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(0, view.tag as Int)
            } else if (id == R.id.imgEditClMyVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(1, view.tag as Int)
            } else if (id == R.id.imgDeleteClMyVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(2, view.tag as Int)
            } else if (id == R.id.imgShareClMyVideo) {
                adapterItemTypeCallback.onItemTypeClickCallback(3, view.tag as Int)
            }
        }

        init {
            llMain = view.findViewById(R.id.llMainClVideoList)
            imgThumb = view.findViewById(R.id.imgThumbClMyVideo)
            imgPlay = view.findViewById(R.id.imgPlayClMyVideo)
            tvDuration = view.findViewById(R.id.tvDurationClMyVideo)
            tvName = view.findViewById(R.id.tvNameClMyVideo)
            tvSize = view.findViewById(R.id.tvSizeClMyVideo)
            imgEdit = view.findViewById(R.id.imgEditClMyVideo)
            imgDelete = view.findViewById(R.id.imgDeleteClMyVideo)
            imgShare = view.findViewById(R.id.imgShareClMyVideo)
            llMain.setOnClickListener(this)
            imgThumb.setOnClickListener(this)
            imgPlay.setOnClickListener(this)
            imgEdit.setOnClickListener(this)
            imgDelete.setOnClickListener(this)
            imgShare.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_video_list, parent, false)
        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        val aClass: VideoClass = videoClassArrayList[position]
        try {
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
        holder.tvName.text = aClass.FileName
        holder.tvSize.text = "Size : " + aClass.FileSize.toString() + " MB"
        holder.llMain.tag = position
        holder.imgThumb.setTag(R.string.adapter_key, position)
        holder.imgPlay.tag = position
        holder.imgEdit.tag = position
        holder.imgDelete.tag = position
        holder.imgShare.tag = position
    }

    override fun getItemCount(): Int {
        return videoClassArrayList.size
    }

    init {
        this.videoClassArrayList = videoClassArrayList
        this.adapterItemTypeCallback = adapterItemTypeCallback
    }
}