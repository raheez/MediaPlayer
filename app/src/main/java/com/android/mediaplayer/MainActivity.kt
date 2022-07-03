package com.android.mediaplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mediaplayer.Adapter.AudioFileAdapter
import com.android.mediaplayer.Interface.AudioFileClickListener
import com.android.mediaplayer.data.AudioFile
import com.android.mediaplayer.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), AudioFileClickListener {

    private var mSelectedPosition: Int = -1
    lateinit var mAdapter: AudioFileAdapter
    lateinit var mainBinding: ActivityMainBinding
    var mAudioFilesList: ArrayList<AudioFile>? = null
    var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initValues()
        setUpRecyclerView()
        initListener()

    }


    private fun initValues() {
        mAudioFilesList = ArrayList<AudioFile>()
    }

    val selectAudioActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                //If multiple audio selected
                if (data?.clipData != null) {
                    val count = data.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val audioUri: Uri? = data.clipData?.getItemAt(i)?.uri
                        Log.d("file_", "_uri_" + audioUri)
                        var mAudioFile = AudioFile()
                        mAudioFile.mUri = audioUri
                        mAudioFile.mFileName = getFilesName(audioUri)
                        mAudioFilesList?.add(mAudioFile)

                    }
                }
                //If single audio file  selected
                else if (data?.data != null) {
                    val mUri: Uri? = data.data
                    var mAudioFile = AudioFile()
                    mAudioFile.mUri = mUri
                    mAudioFile.mFileName = getFilesName(mUri)
                    mAudioFilesList?.add(mAudioFile)
                }

                if (!mAudioFilesList?.isEmpty()!!) {
                    mAdapter?.notifyDataSetChanged()
                }
            }
        }


    private fun setUpRecyclerView() {

        mAdapter = AudioFileAdapter(mAudioFilesList!!, this)
        val mLinearLayoutManager = LinearLayoutManager(this)
        mainBinding?.audioListRv?.layoutManager = mLinearLayoutManager
        mainBinding?.audioListRv?.adapter = mAdapter

    }


    private fun initListener() {

        mainBinding?.buttonStart?.setOnClickListener {
            performStartPauseAction()
        }

        mainBinding?.buttonStop?.setOnClickListener {

            mediaPlayer?.let {

                it.stop()
                it.reset()
                mainBinding?.seekBar?.progress = 0
                mAdapter.updateSelectedState(false)
                mainBinding?.buttonStart?.setImageResource(R.drawable.ic_play_media_player)
            }
        }


        mainBinding?.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {

                    mediaPlayer?.let {

                        var millis = it.duration
                        var total_Sec =
                            TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
                        var mins = TimeUnit.MINUTES.convert(millis.toLong(), TimeUnit.SECONDS)
                        var secs = total_Sec - (mins * 60)

                        var duration = "${mins}:${secs}/ ${it.duration}"

                        mainBinding?.durationTv?.setText(duration)

                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mediaPlayer?.let {
                    mediaPlayer?.seekTo(mainBinding?.seekBar?.progress)
                }

            }
        })

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }

        mainBinding?.buttonAction?.setOnClickListener {
            openMultipleFiles()
        }


        mainBinding?.audioListRv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // Some code when initially scrollState changes
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Some code while the list is scrolling
                val lManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstElementPosition = lManager!!.findFirstVisibleItemPosition()
                val lastElementPosition = lManager!!.findLastVisibleItemPosition()

                /**
                 * logic for pausing audio file when file is not visible
                 */
                if (mSelectedPosition != -1) {
                    if ((mSelectedPosition < firstElementPosition) || (mSelectedPosition > lastElementPosition)) {
                        if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
                            performPauseAction()
                            mAdapter.updateSelectedState(false)
                            mAdapter.notifyDataSetChanged()

                        }
                    }
                }

            }
        })
    }


    private fun performStartPauseAction() {

        mediaPlayer?.let {
            if (it.isPlaying) {
                performPauseAction()
                mAdapter.updateSelectedState(false)

            } else {
                performStartAction()
                mAdapter.updateSelectedState(true)

            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun performPauseAction() {
        mediaPlayer?.pause()
        mainBinding?.buttonStart?.setImageResource(R.drawable.ic_play_media_player)
    }


    private fun performStartAction() {
        mainBinding?.buttonStart?.setImageResource(R.drawable.ic_pause_media_player)
        mediaPlayer?.start()

        val timer = Executors.newScheduledThreadPool(1)
        timer.scheduleAtFixedRate(object : Runnable {
            override fun run() {

                if (mediaPlayer != null) {
                    if (!mainBinding?.seekBar?.isPressed) {
                        mainBinding?.seekBar?.setProgress(mediaPlayer?.currentPosition!!)
                    }
                }
            }
        }, 10, 10, TimeUnit.MILLISECONDS)

    }


    private fun initialiseMediaPlayer(mUri: Uri?) {

        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }

        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {

            setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                setDataSource(
                    applicationContext,
                    mUri!!
                ) //to set media source and send the object to the initialized state
                prepare() //to send the object to prepared state
            } catch (e: Exception) {
                Log.d("audio file", "audio file exception occured")
            }
        }


        var millis = mediaPlayer?.duration ?: 0
        var total_Sec = TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
        var mins = TimeUnit.MINUTES.convert(millis.toLong(), TimeUnit.SECONDS)
        var secs = total_Sec - (mins * 60)

        var duration = "${mins}  :  ${secs}"
        mainBinding?.seekBar?.max = millis
        mainBinding?.seekBar?.setProgress(0)
        mainBinding?.durationTv?.setText("00:00 / ${duration}")

    }


    @SuppressLint("Range")
    private fun getFilesName(mUri: Uri?): String {

        var res = ""
        if (mUri?.scheme.equals("content")) {
            val cursor = contentResolver.query(mUri!!, null, null, null, null)

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                if (res.isEmpty()) {
                    res = mUri.path ?: ""
                    val cut = res.lastIndexOf('/')
                    if (cut != 1) {
                        res = res.substring(cut + 1)
                    }
                }
            }

        }
        return res

    }

    private fun openMultipleFiles() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        //intent.type = "audio/*"
        intent.type = "audio/*"
        selectAudioActivityResult.launch(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            it?.stop()
            it?.reset()
            it.release()
        }
    }

    override fun OnAudionFileClicked(mAudioFile: AudioFile, position: Int) {
        if (mSelectedPosition == position) {
            mSelectedPosition = position
            mainBinding?.audioListRv?.smoothScrollToPosition(mSelectedPosition)
            performStartPauseAction()
        } else {

            mSelectedPosition = position
            mainBinding?.audioListRv?.smoothScrollToPosition(mSelectedPosition)
            initialiseMediaPlayer(mAudioFile.mUri)

            performStartPauseAction()
            if (!mainBinding?.mediaPlayerLayout?.isVisible) {
                mainBinding?.mediaPlayerLayout?.visibility = View.VISIBLE
            }

        }



        mainBinding?.fileNameTv?.setText(mAudioFile?.mFileName)
    }


}