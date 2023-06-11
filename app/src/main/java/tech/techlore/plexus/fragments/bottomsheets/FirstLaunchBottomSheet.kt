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

import android.animation.Animator
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.databinding.BottomSheetFirstLaunchBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_LAUNCH

class FirstLaunchBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetFirstLaunchBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        (dialog as BottomSheetDialog).apply {
            setCanceledOnTouchOutside(false)
            behavior.isDraggable = false
            
            // Prevent bottom sheet dismiss on back pressed
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Do nothing
                    return@OnKeyListener true
                }
                false
            })
        }
        
        _binding = BottomSheetFirstLaunchBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Anim view
        bottomSheetBinding.helloAnimView.apply {
            setMaxFrame(300)
            
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                
                override fun onAnimationEnd(animation: Animator) {
                    val fadeIn = AlphaAnimation(0.5f, 1.0f)
                    fadeIn.duration = 600
                    bottomSheetBinding.welcomeTextDesc.apply {
                        isVisible = true
                        startAnimation(fadeIn)
                    }
                    bottomSheetBinding.proceedButton.apply {
                        isVisible = true
                        startAnimation(fadeIn)
                    }
                    bottomSheetBinding.skipButton.apply {
                        isVisible = true
                        startAnimation(fadeIn)
                    }
                }
                
                override fun onAnimationCancel(animation: Animator) {}
                
                override fun onAnimationRepeat(animation: Animator) {}
            })
            
            playAnimation()
        }
        
        // Proceed
        bottomSheetBinding.proceedButton.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingsActivity::class.java)
                              .putExtra("frag", R.id.helpVideosFragment))
            requireActivity().finish()
        }
        
        // Skip
        bottomSheetBinding.skipButton.setOnClickListener {
            PreferenceManager(requireActivity()).setBoolean(FIRST_LAUNCH, false)
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}