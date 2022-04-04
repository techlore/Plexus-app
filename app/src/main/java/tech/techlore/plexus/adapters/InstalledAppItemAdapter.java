package tech.techlore.plexus.adapters;

import static tech.techlore.plexus.utils.Utility.RatingColor;
import static tech.techlore.plexus.utils.Utility.hScrollText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.fastscroll.PopupTextProvider;
import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;

public class InstalledAppItemAdapter extends RecyclerView.Adapter<InstalledAppItemAdapter.ListViewHolder>
        implements Filterable, PopupTextProvider {

    private final List<InstalledApp> aListViewItems;
    private final List<InstalledApp> aListViewItemsFull;
    private InstalledAppItemAdapter.OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(InstalledAppItemAdapter.OnItemClickListener clickListener) {
        itemClickListener = clickListener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView name, packageName, installedVersion, dgRating, mgRating;
        private final ImageView versionMismatch;

        public ListViewHolder(@NonNull View itemView, InstalledAppItemAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            packageName = itemView.findViewById(R.id.package_name);
            installedVersion = itemView.findViewById(R.id.version);
            dgRating = itemView.findViewById(R.id.dg_rating);
            mgRating = itemView.findViewById(R.id.mg_rating);
            versionMismatch = itemView.findViewById(R.id.version_mismatch);


            // HANDLE CLICK EVENTS OF ITEMS
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });

        }
    }

    public InstalledAppItemAdapter(List<InstalledApp> listViewItems)
    {
        aListViewItems = listViewItems;
        aListViewItemsFull = new ArrayList<>(aListViewItems);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public InstalledAppItemAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new InstalledAppItemAdapter.ListViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final InstalledAppItemAdapter.ListViewHolder holder, int position) {

        final InstalledApp installedApp = aListViewItems.get(position);
        final Context context = holder.itemView.getContext();

        if (!installedApp.getInstalledVersion().equals(installedApp.getPlexusVersion())){
            holder.versionMismatch.setVisibility(View.VISIBLE);
        }
        else {
            holder.versionMismatch.setVisibility(View.GONE);
        }

        // SET APP NAME, PACKAGE NAME, VERSION, SCORES
        holder.name.setText(installedApp.getName());
        holder.packageName.setText(installedApp.getPackageName());
        holder.installedVersion.setText(installedApp.getInstalledVersion());
        holder.dgRating.setText(installedApp.getDgRating());
        holder.mgRating.setText(installedApp.getMgRating());

        // SET HORIZONTALLY SCROLLING TEXT
        hScrollText(holder.name);
        hScrollText(holder.packageName);
        hScrollText(holder.installedVersion);

        // SET RATING BACKGROUND COLOR
        RatingColor(context, holder.dgRating, installedApp.getDgRating());
        RatingColor(context, holder.mgRating, installedApp.getMgRating());

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
                List<InstalledApp> filteredList = new ArrayList<>();

                if (charSequence != null) {

                    String searchString = charSequence.toString().toLowerCase().trim();

                    for (InstalledApp installedApp: aListViewItemsFull){

                        if (installedApp.getName().toLowerCase().contains(searchString)
                                || installedApp.getPackageName().toLowerCase().contains(searchString)){

                            filteredList.add(installedApp);
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
                aListViewItems.addAll((ArrayList<InstalledApp>) filterResults.values);
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
