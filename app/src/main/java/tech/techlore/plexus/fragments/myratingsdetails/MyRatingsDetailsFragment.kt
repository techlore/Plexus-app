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

package tech.techlore.plexus.fragments.myratingsdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.myratingsdetails.MyRatingsDetailsItemAdapter
import tech.techlore.plexus.databinding.FragmentRatingsDetailsBinding

class MyRatingsDetailsFragment : Fragment() {
    
    private var _binding: FragmentRatingsDetailsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentRatingsDetailsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val detailsActivity = requireActivity() as MyRatingsDetailsActivity
        
        if (!detailsActivity.isListSorted) detailsActivity.sortMyRatingsDetails()
        
        if (detailsActivity.sortedMyRatingsDetailsList.isEmpty()) {
            fragmentBinding.emptyRatingsListViewStub.inflate()
            val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.emptyListViewText)
            emptyListView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            emptyListView.text = requireContext().getString(R.string.no_ratings_available)
        }
        else {
            val myRatingsItemAdapter = MyRatingsDetailsItemAdapter(detailsActivity.sortedMyRatingsDetailsList)
            fragmentBinding.userRatingsRv.adapter = myRatingsItemAdapter
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}