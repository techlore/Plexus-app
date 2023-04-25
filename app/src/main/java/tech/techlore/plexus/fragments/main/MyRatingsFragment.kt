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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.adapters.main.MyRatingItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.fragments.dialogs.NoNetworkDialog
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils
import kotlin.coroutines.CoroutineContext

class MyRatingsFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener,
    CoroutineScope {
    
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var myRatingItemAdapter: MyRatingItemAdapter
    private lateinit var myRatingsList: ArrayList<MyRating>
    private lateinit var preferenceManager: PreferenceManager
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
        
        preferenceManager = PreferenceManager(requireContext())
        mainActivity = requireActivity() as MainActivity
        myRatingsRepository = (requireContext().applicationContext as ApplicationManager).myRatingsRepository
        runBlocking {
            launch {
                myRatingsList = myRatingsRepository.getSortedMyRatings()
            }
        }
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
        
        if (myRatingsList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            myRatingItemAdapter = MyRatingItemAdapter(myRatingsList)
            fragmentBinding.recyclerView.adapter = myRatingItemAdapter
            FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(
            R.color.color_background, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener { refreshData() }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val myRating = myRatingsList[position]
        startDetailsActivity(mainActivity, myRating.packageName!!)
    }
    
    private fun refreshData() {
        
        launch {
            if (NetworkUtils.hasNetwork(requireContext()) && NetworkUtils.hasInternet()) {
                myRatingItemAdapter.updateList(myRatingsRepository.getSortedMyRatings())
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
            }
            else {
                NoNetworkDialog(negativeButtonText = getString(R.string.cancel),
                                positiveButtonClickListener = { refreshData() },
                                negativeButtonClickListener = {
                                    fragmentBinding.swipeRefreshLayout.isRefreshing = false
                                })
                    .show(parentFragmentManager, "NoNetworkDialog")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
        _binding = null
    }
}