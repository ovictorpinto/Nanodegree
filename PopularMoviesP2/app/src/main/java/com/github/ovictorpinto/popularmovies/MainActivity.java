package com.github.ovictorpinto.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.popularmovies.model.Movie;

public class MainActivity extends AppCompatActivity implements MainFragment.DetailListener {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getResources().getBoolean(R.bool.needDual) && savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.detail_layout, new EmptyFrag()).commit();
        }
    }
    
    @Override
    public void onSelected(Movie movie) {
        if (getResources().getBoolean(R.bool.needDual)) {
            Fragment fragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Movie.PARAM_ID, movie.getId());
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.detail_layout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra(Movie.PARAM_ID, movie.getId());
            startActivity(intent);
        }
    }
    
    public static class EmptyFrag extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.ly_empty_movie, null, false);
        }
    }
}
