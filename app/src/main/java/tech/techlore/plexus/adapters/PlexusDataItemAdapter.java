package tech.techlore.plexus.adapters;

import static tech.techlore.plexus.utils.Utility.ScoreColor;

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

import tech.techlore.plexus.R;
import tech.techlore.plexus.models.PlexusData;

public class PlexusDataItemAdapter extends RecyclerView.Adapter<PlexusDataItemAdapter.ListViewHolder> implements Filterable {

    private final List<PlexusData> aListViewItems;
    private final List<PlexusData> aListViewItemsFull;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        itemClickListener = clickListener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView name, packageName, version, dgRating, mgRating;

        public ListViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            packageName = itemView.findViewById(R.id.package_name);
            version = itemView.findViewById(R.id.version);
            dgRating = itemView.findViewById(R.id.dg_rating);
            mgRating = itemView.findViewById(R.id.mg_rating);


            // HANDLE CLICK EVENTS OF ITEMS
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position=getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
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
        return new PlexusDataItemAdapter.ListViewHolder(view, itemClickListener);
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
        ScoreColor(context, holder.dgRating, plexusData.dgRating);
        ScoreColor(context, holder.mgRating, plexusData.mgRating);

    }

    // HORIZONTALLY SCROLL TEXT
    // IF TEXT IS TOO LONG
    private void hScrollText(TextView textView) {

        // SET THESE 2 PARAMETERS FOR HORIZONTALLY SCROLLING TEXT
        textView.setSingleLine();
        textView.setSelected(true);
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

}
