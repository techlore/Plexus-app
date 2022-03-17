package tech.techlore.plexus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

        if (args  != null) {

            name.setText(args.getString("name"));
            packageName.setText(args.getString("packageName"));
            version.setText(args.getString("version"));
            dgNotes.setText(args.getString("dgNotes"));
            mgNotes.setText(args.getString("mgNotes"));
            dgRating.setText(args.getString("dgRating"));
            mgRating.setText(args.getString("mgRating"));

            ScoreColor(dgRatingColor, args.getString("dgRating"));
            ScoreColor(mgRatingColor, args.getString("mgRating"));

        }

    }

    // SET BACKGROUND COLOR BASED ON SCORES
    private void ScoreColor(ImageView imageView, String score) {

        switch (score) {

            case "X":
                imageView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.ratingXColor));
                break;

            case "1":
                imageView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.rating1Color));
                break;

            case "2":
                imageView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.rating2Color));
                break;

            case "3":
                imageView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.rating3Color));
                break;

            case "4":
                imageView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.rating4Color));
                break;

        }

    }

}
