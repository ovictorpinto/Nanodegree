package com.github.ovictorpinto.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.github.ovictorpinto.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by victorpinto on 18/10/16. 
 */

public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Context context, List<Movie> objects) {
        super(context, View.NO_ID, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.ly_item_movie, null);
        }

        Movie movie = getItem(position);

        ImageView imageview = (ImageView) convertView.findViewById(R.id.imageview);
        Picasso.with(getContext()).load(movie.getFullPath()).into(imageview);

        return convertView;
    }
}
