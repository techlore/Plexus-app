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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.FavoriteItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge

class FavoritesFragment:
    Fragment(),
    FavoriteItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var favItemAdapter: FavoriteItemAdapter
    private lateinit var favList: ArrayList<MainDataMinimal>
    private val prefManager by inject<PreferenceManager>()
    private val miniRepository by inject<MainDataMinimalRepository>()
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        
        // Adjust recycler view for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        
        lifecycleScope.launch{
            favList =
                miniRepository.miniFavListFromDB(installedFromPref = prefManager.getInt(INSTALLED_FROM_SORT),
                                                 statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
                                                 orderPref = prefManager.getInt(A_Z_SORT))
    
            if (favList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
            }
            else {
                favItemAdapter = FavoriteItemAdapter(favList,
                                                     this@FavoritesFragment,
                                                     lifecycleScope)
                fragmentBinding.recyclerView.apply {
                    mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
                    adapter = favItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
            }
    
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.isEnabled = false
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isDataUpdated) {
            lifecycleScope.launch{
                favItemAdapter
                    .updateList(miniRepository
                                    .miniFavListFromDB(installedFromPref = prefManager.getInt(INSTALLED_FROM_SORT),
                                                       statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
                                                       orderPref = prefManager.getInt(A_Z_SORT)))
                DataState.isDataUpdated = false
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val fav = favList[position]
        mainActivity.startDetailsActivity(fav.packageName)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}