package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.utils.Utility.ScoreColor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tech.techlore.plexus.R;

public class AppDetailsFragment extends Fragment {

    public AppDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_app_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        TextView name = view.findViewById(R.id.name_details);
        TextView packageName = view.findViewById(R.id.package_name_details);
        TextView version = view.findViewById(R.id.version_details);
        TextView dgNotes = view.findViewById(R.id.dg_notes);
        TextView mgNotes = view.findViewById(R.id.mg_notes);
        TextView dgRating = view.findViewById(R.id.dg_rating_details);
        TextView mgRating = view.findViewById(R.id.mg_rating_details);
        ImageView dgRatingColor = view.findViewById(R.id.dg_rating_color);
        ImageView mgRatingColor = view.findViewById(R.id.mg_rating_color);

    /*###########################################################################################*/

        if (args  != null) {

            name.setText(args.getString("name"));
            packageName.setText(args.getString("packageName"));
            version.setText(args.getString("version"));
            dgNotes.setText(args.getString("dgNotes"));
            mgNotes.setText(args.getString("mgNotes"));
            dgRating.setText(args.getString("dgRating"));
            mgRating.setText(args.getString("mgRating"));

            ScoreColor(requireContext(), dgRatingColor, args.getString("dgRating"));
            ScoreColor(requireContext(), mgRatingColor, args.getString("mgRating"));

        }

    }

}
