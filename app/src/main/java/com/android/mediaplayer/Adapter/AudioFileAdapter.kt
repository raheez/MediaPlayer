package com.android.mediaplayer.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.mediaplayer.Interface.AudioFileClickListener
import com.android.mediaplayer.R
import com.android.mediaplayer.data.AudioFile
import com.android.mediaplayer.databinding.AdapterAudioListItemRecyclerViewBinding

class AudioFileAdapter(mListParams: ArrayList<AudioFile>, mAudioClickListenerParam: AudioFileClickListener) :
    RecyclerView.Adapter<AudioFileAdapter.AudioFileViewHolder>() {


    var mList = mListParams
    var mAudioClickListenerParam = mAudioClickListenerParam
    var mSelectedPosition = -1
    var mIsPlaying = false

    class AudioFileViewHolder(val binding: AdapterAudioListItemRecyclerViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioFileViewHolder {
        val view = AdapterAudioListItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AudioFileViewHolder(view)

    }

    override fun onBindViewHolder(holder: AudioFileViewHolder, position: Int) {

        holder?.binding?.audioFileTitle?.setText(mList.get(position).mFileName)
        holder?.binding?.audioActionButton?.setOnClickListener{
            mAudioClickListenerParam?.OnAudionFileClicked(mList?.get(position),position)
            mSelectedPosition = position
        }

        if (mSelectedPosition!=-1 && mSelectedPosition == position && mIsPlaying){
            holder.binding?.audioActionButton?.setImageResource(R.drawable.ic_list_pause)
        }else{
            holder.binding?.audioActionButton?.setImageResource(R.drawable.ic_list_play)
        }

    }

     fun updateSelectedState(mIsPlayingParam : Boolean) {
        mIsPlaying = mIsPlayingParam
    }


    override fun getItemCount(): Int {
        return mList.size
    }
}