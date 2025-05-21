/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.bottomsheets.appdetails

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetTranslateRatingNoteBinding
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import java.util.Locale

class TranslateRatingNoteBottomSheet(private val notes: String) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetTranslateRatingNoteBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private val footerBinding get() = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
    
    private companion object {
        private const val FADE_ANIM_DURATION = 400L
        private val SHOW_ANIM_INTERPOLATOR = DecelerateInterpolator()
        private val HIDE_ANIM_INTERPOLATOR = AccelerateInterpolator()
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetTranslateRatingNoteBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        retrieveTranslation()
        
        footerBinding.apply {
            positiveButton.isVisible = false
            negativeButton.setOnClickListener { dismiss() }
        }
    }
    
    private fun retrieveTranslation() {
        lifecycleScope.launch {
            if (hasNetwork(requireContext()) && hasInternet()) {
                withContext(Dispatchers.IO) {
                    val translateResponse =
                        get<ApiRepository>().translateRatingNote(notes, Locale.getDefault().language)
                    
                    when (translateResponse.status.value) {
                        200 -> {
                            val responseBody = translateResponse.bodyAsText()
                            val jsonElement = Json.parseToJsonElement(responseBody)
                            val translatedText = jsonElement.jsonObject["translatedText"]?.jsonPrimitive?.content
                            withContext(Dispatchers.Main) {
                                bottomSheetBinding.apply {
                                    arrayOf(translateLoadingIndicator, translatingTextView).forEach {
                                        hideViewWithAnimation(it)
                                    }
                                    showViewWithAnimation(ratingsNotesCard)
                                    noteOriginal.text = notes
                                    noteTranslated.text = translatedText
                                }
                            }
                        }
                        else -> withContext(Dispatchers.Main) {
                            bottomSheetBinding.apply {
                                hideViewWithAnimation(translateLoadingIndicator)
                                translatingTextView.text = getString(R.string.error_translating)
                            }
                        }
                        
                    }
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { retrieveTranslation() },
                                     negativeButtonClickListener = { dismiss() })
                    .show(parentFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private fun showViewWithAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = FADE_ANIM_DURATION
            interpolator = SHOW_ANIM_INTERPOLATOR
            start()
        }.doOnEnd {
            view.isVisible = true
            footerBinding.negativeButton.text = getString(R.string.dismiss)
        }
    }
    
    private fun hideViewWithAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = FADE_ANIM_DURATION
            interpolator = HIDE_ANIM_INTERPOLATOR
            start()
        }.doOnEnd { view.isVisible = false }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}