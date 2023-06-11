/*
 * Copyright (c) 2022-present Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.fragments.bottomsheets

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.VideoView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetVideoPlayerBinding
import tech.techlore.plexus.preferences.PreferenceManager

class VideoPlayerBottomSheet(private val videoId: Int) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetVideoPlayerBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.behavior.isDraggable = false
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        
        // Prevent bottom sheet dismiss on back pressed
        bottomSheetDialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // Do nothing
                return@OnKeyListener true
            }
            false
        })
        
        _binding = BottomSheetVideoPlayerBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val videoUri = Uri.parse("android.resource://tech.techlore.plexus/${videoId}")
        val preferenceManager = PreferenceManager(requireContext())
        
        /*####################################################################################*/
        
        bottomSheetBinding.videoTitle.text =
            when (videoId) {
                R.raw.intro_video -> getString(R.string.introduction)
                R.raw.navigation_video -> getString(R.string.navigation)
                R.raw.submissions_video -> getString(R.string.submissions)
                else -> getString(R.string.about)
            }
        
        // Video
        bottomSheetBinding.videoView.apply {
            setVideoURI(videoUri)
            
            setOnPreparedListener { mediaPlayer ->
                keepScreenOn = true
                bottomSheetBinding.seekBar.max = mediaPlayer.duration
                val durationInSeconds = mediaPlayer.duration / 1000
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60
                bottomSheetBinding.maxTime.text = String.format("%02d:%02d", minutes, seconds)
                updateSeekBar(this)
            }
            
            setOnClickListener {
                if (bottomSheetBinding.seekBar.progress != bottomSheetBinding.seekBar.max) {
                    if (isPlaying) {
                        pause()
                        bottomSheetBinding.startChronometer.stop()
                        keepScreenOn = false
                    }
                    else {
                        start()
                        bottomSheetBinding.startChronometer.base = SystemClock.elapsedRealtime() - currentPosition
                        bottomSheetBinding.startChronometer.start()
                        keepScreenOn = true
                    }
                }
            }
            
            setOnCompletionListener {
                keepScreenOn = false
                bottomSheetBinding.seekBar.progress = bottomSheetBinding.seekBar.max
                bottomSheetBinding.startChronometer.base = SystemClock.elapsedRealtime() - bottomSheetBinding.seekBar.max
                bottomSheetBinding.startChronometer.stop()
                if (preferenceManager.getBoolean(PreferenceManager.FIRST_LAUNCH)) {
                    dismiss()
                    FirstLaunchBottomSheet().show(parentFragmentManager, "FirstLaunchBottomSheet")
                }
            }
        }
        
        // Seekbar
        bottomSheetBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    bottomSheetBinding.videoView.seekTo(progress)
                    bottomSheetBinding.startChronometer.base = SystemClock.elapsedRealtime() - progress
                    updateSeekBar(bottomSheetBinding.videoView)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        
        bottomSheetBinding.dismissBtn.setOnClickListener {
            dismiss()
            if (preferenceManager.getBoolean(PreferenceManager.FIRST_LAUNCH)) {
                FirstLaunchBottomSheet().show(parentFragmentManager, "FirstLaunchBottomSheet")
            }
        }
        
        bottomSheetBinding.videoView.start()
        bottomSheetBinding.startChronometer.start()
    }
    
    private fun updateSeekBar(videoView: VideoView) {
        lifecycleScope.launch {
            while (bottomSheetBinding.seekBar.progress < bottomSheetBinding.seekBar.max) {
                if (!isActive) {
                    break
                }
                else if (videoView.isPlaying) {
                    bottomSheetBinding.seekBar.progress = videoView.currentPosition
                    bottomSheetBinding.startChronometer.base =
                        SystemClock.elapsedRealtime() - videoView.currentPosition
                }
                delay(500) // Update seekbar every 500 milliseconds
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bottomSheetBinding.videoView.apply {
            stopPlayback()
            keepScreenOn = false
        }
        _binding = null
    }
}