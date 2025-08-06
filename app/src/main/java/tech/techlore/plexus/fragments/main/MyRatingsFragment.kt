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

package tech.techlore.plexus.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.main.MyRatingsItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_SUBMISSION
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startActivityWithTransition
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import kotlin.getValue

class MyRatingsFragment :
    Fragment(),
    MyRatingsItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var myRatingsItemAdapter: MyRatingsItemAdapter
    private lateinit var myRatingsList: ArrayList<MyRating>
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        val prefManager by inject<PreferenceManager>()
        val myRatingsRepository by inject<MyRatingsRepository>()
        
        // Adjust UI components for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        ViewCompat.setOnApplyWindowInsetsListener(mainActivity.activityBinding.newRatingFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left + convertDpToPx(requireContext(), 16f)
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 95f)
            }
            
            WindowInsetsCompat.CONSUMED
        }
        
        lifecycleScope.launch{
            myRatingsList =
                myRatingsRepository.getSortedMyRatingsByName(orderPref = prefManager.getInt(A_Z_SORT))
            
            if (myRatingsList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_ratings)
                fragmentBinding.root.findViewById<MaterialTextView>(R.id.emptyListViewText).apply {
                        setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                        text =
                            if (prefManager.getBoolean(IS_FIRST_SUBMISSION)) {
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
                myRatingsItemAdapter =
                    MyRatingsItemAdapter(
                        aListViewItems = myRatingsList,
                        clickListener = this@MyRatingsFragment,
                        isGridView = mainActivity.isGridView
                    )
                fragmentBinding.recyclerView.apply {
                    mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
                    layoutManager =
                        if (!mainActivity.isGridView)
                            LinearLayoutManager(requireContext())
                        else
                            GridLayoutManager(requireContext(), 2)
                    adapter = myRatingsItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
            }
            
            // New rating FAB
            mainActivity.activityBinding.newRatingFab.apply {
                show()
                setOnClickListener {
                    mainActivity.onNavViewItemSelected(R.id.nav_installed_apps, false)
                }
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.isEnabled = false
        }
        
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val myRating = myRatingsList[position]
        mainActivity.apply {
            startActivityWithTransition(Intent(this, MyRatingsDetailsActivity::class.java)
                                            .putExtra("packageName", myRating.packageName))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mainActivity.activityBinding.newRatingFab.apply {
            if (isShown) hide()
        }
    }
}