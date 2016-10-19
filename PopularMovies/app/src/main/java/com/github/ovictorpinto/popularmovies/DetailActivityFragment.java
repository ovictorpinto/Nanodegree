package com.github.ovictorpinto.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import static com.github.ovictorpinto.popularmovies.R.id.imageview;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_detail, container, false);

        Movie movie = getActivity().getIntent().getParcelableExtra(Movie.PARAM);

        TextView textViewTitle = (TextView) mainView.findViewById(R.id.textview_title);
        textViewTitle.setText(movie.getTitle());

        TextView textViewOverview = (TextView) mainView.findViewById(R.id.textview_overview);
        textViewOverview.setText(movie.getOverview());

        TextView textViewVote = (TextView) mainView.findViewById(R.id.textview_vote);
        String vote = " - ";
        if (movie.getVoteAverage() != null) {
            vote = movie.getVoteAverage().toString();
        }
        textViewVote.setText(vote);

        TextView textViewDate = (TextView) mainView.findViewById(R.id.textview_date);
        String date = " - ";
        if (movie.getRelease() != null) {
            date = DateFormat.getDateFormat(getContext()).format(movie.getRelease());
        }
        textViewDate.setText(date);

        ImageView imageView = (ImageView) mainView.findViewById(imageview);
        Picasso.with(getContext()).load(movie.getFullPath()).into(imageView);

        return mainView;
    }
}
