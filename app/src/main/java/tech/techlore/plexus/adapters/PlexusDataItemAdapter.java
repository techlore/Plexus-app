/*
 * Copyright (c) 2022 Techlore
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

package tech.techlore.plexus.adapters;

import static tech.techlore.plexus.utils.UiUtils.RatingColor;
import static tech.techlore.plexus.utils.UiUtils.hScrollText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.fastscroll.PopupTextProvider;
import tech.techlore.plexus.R;
import tech.techlore.plexus.models.PlexusData;

public class PlexusDataItemAdapter extends RecyclerView.Adapter<PlexusDataItemAdapter.ListViewHolder>
        implements Filterable, PopupTextProvider {

    private final List<PlexusData> aListViewItems;
    private final List<PlexusData> aListViewItemsFull;
    private OnItemClickListener itemClickListener;
    private OnItemLongCLickListener itemLongCLickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongCLickListener {
        void onItemLongCLick (int position);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        itemClickListener = clickListener;
    }

    public void setOnItemLongClickListener(OnItemLongCLickListener longClickListener) {
        itemLongCLickListener=longClickListener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView name, packageName, version, dgRating, mgRating;

        public ListViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener, OnItemLongCLickListener onItemLongCLickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            packageName = itemView.findViewById(R.id.package_name);
            version = itemView.findViewById(R.id.version);
            dgRating = itemView.findViewById(R.id.dg_rating);
            mgRating = itemView.findViewById(R.id.mg_rating);


            // HANDLE CLICK EVENTS OF ITEMS
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });

            // HANDLE LONG CLICK EVENTS OF ITEMS
            itemView.setOnLongClickListener(v -> {
                if (onItemLongCLickListener != null) {
                    int position=getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemLongCLickListener.onItemLongCLick(position);
                    }
                }
                return true;
            });

        }
    }

    public PlexusDataItemAdapter(List<PlexusData> listViewItems)
    {
        aListViewItems = listViewItems;
        aListViewItemsFull = new ArrayList<>(aListViewItems);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public PlexusDataItemAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new PlexusDataItemAdapter.ListViewHolder(view, itemClickListener, itemLongCLickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlexusDataItemAdapter.ListViewHolder holder, int position) {

        final PlexusData plexusData = aListViewItems.get(position);
        final Context context = holder.itemView.getContext();

        // SET APP NAME, PACKAGE NAME, VERSION, SCORES
        holder.name.setText(plexusData.name);
        holder.packageName.setText(plexusData.packageName);
        holder.version.setText(plexusData.version);
        holder.dgRating.setText(plexusData.dgRating);
        holder.mgRating.setText(plexusData.mgRating);

        // SET HORIZONTALLY SCROLLING TEXT
        hScrollText(holder.name);
        hScrollText(holder.packageName);
        hScrollText(holder.version);

        // SET SCORE BACKGROUND COLOR
        RatingColor(context, holder.dgRating, plexusData.dgRating);
        RatingColor(context, holder.mgRating, plexusData.mgRating);

    }

    @Override
    public int getItemCount() {
        return aListViewItems.size();
    }

    @Override
    public  int getItemViewType(int position){
        return position;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    // REQUIRED FOR SEARCH
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<PlexusData> filteredList = new ArrayList<>();

                if (charSequence != null) {

                    String searchString = charSequence.toString().toLowerCase().trim();

                    for (PlexusData plexusData : aListViewItemsFull){

                        if (plexusData.name.toLowerCase().contains(searchString)
                            || plexusData.packageName.toLowerCase().contains(searchString)){

                            filteredList.add(plexusData);
                        }
                    }

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                aListViewItems.clear();
                //noinspection unchecked
                aListViewItems.addAll((ArrayList<PlexusData>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    // FAST SCROLL POPUP
    @NonNull
    @Override
    public String getPopupText(int position) {
        return aListViewItems.get(position).name.substring(0,1);
    }

}
