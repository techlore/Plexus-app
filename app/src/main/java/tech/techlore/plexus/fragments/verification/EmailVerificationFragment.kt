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

package tech.techlore.plexus.fragments.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doBeforeTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.FragmentEmailVerificationBinding
import tech.techlore.plexus.fragments.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.verification.EmailVerificationBottomSheet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmail
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis

class EmailVerificationFragment : Fragment() {
    
    private var _binding: FragmentEmailVerificationBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        var job: Job? = null
        
        fragmentBinding.emailText.doBeforeTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job =
                lifecycleScope.launch {
                    delay(200)
                    fragmentBinding.proceedBtn.isEnabled =
                        charSequence!!.isNotEmpty()
                        && !hasBlockedWord(requireContext(), charSequence)
                        && !hasEmojis(charSequence)
                        && hasEmail(charSequence)
                }
        }
        
        // Proceed
        fragmentBinding.proceedBtn.apply {
            setOnClickListener {
                lifecycleScope.launch{
                    if (hasNetwork(requireContext()) && hasInternet()) {
                        EmailVerificationBottomSheet(fragmentBinding.emailText.text.toString())
                            .show(parentFragmentManager, "VerificationBottomSheet")
                    }
                    else {
                        NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                             positiveButtonClickListener = {
                                                 EmailVerificationBottomSheet(fragmentBinding.emailText.text.toString())
                                                     .show(parentFragmentManager, "VerificationBottomSheet")
                                             },
                                             negativeButtonClickListener = {})
                            .show(parentFragmentManager, "NoNetworkBottomSheet")
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}