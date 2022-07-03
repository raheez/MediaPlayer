package com.android.mediaplayer.Interface

import com.android.mediaplayer.data.AudioFile

interface AudioFileClickListener {

    fun OnAudionFileClicked(mAudioFile : AudioFile, position:Int)
}