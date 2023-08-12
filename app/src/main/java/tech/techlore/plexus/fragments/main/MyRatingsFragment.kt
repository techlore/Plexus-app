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

package tech.techlore.plexus.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.main.MyRatingsItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_SUBMISSION
import tech.techlore.plexus.repositories.database.MyRatingsRepository

class MyRatingsFragment :
    Fragment(),
    MyRatingsItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var myRatingsItemAdapter: MyRatingsItemAdapter
    private lateinit var myRatingsList: ArrayList<MyRating>
    private lateinit var myRatingsRepository: MyRatingsRepository
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        myRatingsRepository = (requireContext().applicationContext as ApplicationManager).myRatingsRepository
        
        lifecycleScope.launch{
            myRatingsList = myRatingsRepository.getSortedMyRatingsByName(orderPref = PreferenceManager(requireContext()).getInt(A_Z_SORT))
            
            if (myRatingsList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_ratings)
                fragmentBinding.root.findViewById<MaterialTextView>(R.id.emptyListViewText)
                    .apply {
                        setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                        text =
                            if (PreferenceManager(requireContext()).getBoolean(IS_FIRST_SUBMISSION)) {
                                getString(R.string.no_ratings_available) +
                                "\n\n" +
                                getString(R.string.submit_first_rating)
                            }
                            else {
                                getString(R.string.no_ratings_available)
                            }
                    }
            }
            else {
                myRatingsItemAdapter = MyRatingsItemAdapter(myRatingsList, this@MyRatingsFragment)
                fragmentBinding.recyclerView.apply {
                    addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
                    adapter = myRatingsItemAdapter
                    FastScrollerBuilder(this).useMd2Style().build() // Fast scroll
                }
            }
            
            // New rating fab
            fragmentBinding.newRatingFab.apply {
                isVisible = true
                setOnClickListener {
                    mainActivity.selectedNavItem = R.id.nav_submit_rating
                    mainActivity.clickedNavItem = R.id.nav_submit_rating
                    mainActivity.displayFragment(mainActivity.clickedNavItem)
                }
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.isEnabled = false
        }
        
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val myRating = myRatingsList[position]
        startActivity(Intent(requireActivity(), MyRatingsDetailsActivity::class.java)
                          .putExtra("packageName", myRating.packageName))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}