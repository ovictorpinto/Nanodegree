package com.github.ovictorpinto.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ovictorpinto.popularmovies.model.Movie;
import com.github.ovictorpinto.popularmovies.model.MovieDBResult;
import com.github.ovictorpinto.popularmovies.model.Review;
import com.github.ovictorpinto.popularmovies.model.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Documento completo https://developers.themoviedb.org/3/movies
 * Exemplo; https://api.themoviedb.org/3/movie/popular?api_key=
 * Exemplo; https://api.themoviedb.org/3/movie/321612/videos?api_key=
 */
public class DetailActivityFragment extends Fragment {
    
    private MovieDBService service;
    private Movie movie;
    private List<Review> reviews;
    private List<Video> videos;
    private RecyclerView recyclerView;
    private View progress;
    
    public DetailActivityFragment() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.ly_detail, container, false);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        
        progress = mainView.findViewById(R.id.progress);
        movie = getActivity().getIntent().getParcelableExtra(Movie.PARAM);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        JacksonConverterFactory jacksonConverterFactory = JacksonConverterFactory.create(objectMapper);
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/").addConverterFactory(jacksonConverterFactory)
                                                  .build();
        
        service = retrofit.create(MovieDBService.class);
        
        callDetail();
        
        return mainView;
    }
    
    //chamada só pra atualizar o runtime do filme
    private void callDetail() {
        Call<Movie> call = service.getDetails(movie.getId(), BuildConfig.MOVIE_DB_API_KEY);
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Movie result = response.body();
                    movie.setRuntime(result.getRuntime());
                } else {
                    System.out.println(response.errorBody());
                }
                callReviews();
            }
            
            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(getActivity(), R.string.failed_get_details, Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                callReviews();
            }
        });
    }
    
    private void callReviews() {
        Call<MovieDBResult<Review>> call = service.listReviews(movie.getId(), BuildConfig.MOVIE_DB_API_KEY);
        call.enqueue(new Callback<MovieDBResult<Review>>() {
            @Override
            public void onResponse(Call<MovieDBResult<Review>> call, Response<MovieDBResult<Review>> response) {
                if (response.isSuccessful()) {
                    MovieDBResult<Review> result = response.body();
                    reviews = result.getResults();
                } else {
                    System.out.println(response.errorBody());
                }
                callVideo();
            }
            
            @Override
            public void onFailure(Call<MovieDBResult<Review>> call, Throwable t) {
                Toast.makeText(getActivity(), R.string.failed_get_reviews, Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                callVideo();
            }
        });
    }
    
    private void callVideo() {
        Call<MovieDBResult<Video>> call = service.listVideos(movie.getId(), BuildConfig.MOVIE_DB_API_KEY);
        call.enqueue(new Callback<MovieDBResult<Video>>() {
            @Override
            public void onResponse(Call<MovieDBResult<Video>> call, Response<MovieDBResult<Video>> response) {
                if (response.isSuccessful()) {
                    MovieDBResult<Video> result = response.body();
                    videos = result.getResults();
                } else {
                    System.out.println(response.errorBody());
                }
                fillFields();
            }
            
            @Override
            public void onFailure(Call<MovieDBResult<Video>> call, Throwable t) {
                Toast.makeText(getActivity(), R.string.failed_get_videos, Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                fillFields();
            }
        });
    }
    
    private void fillFields() {
        DetailRecyclerAdapter.VideoClickListener movieClickListener = new DetailRecyclerAdapter.VideoClickListener() {
            @Override
            public void onClick(Video video) {
                String youtubeLink = "http://www.youtube.com/watch?v=%s";
                String uri = String.format(youtubeLink, video.getKey());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        };
        recyclerView.setAdapter(new DetailRecyclerAdapter(getActivity(), videos, reviews, movie, movieClickListener));
        recyclerView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }
    
    public interface MovieDBService {
        @GET("movie/{movie}/reviews")
        Call<MovieDBResult<Review>> listReviews(@Path("movie") int idMovie, @Query("api_key") String apiKey);
        
        @GET("movie/{movie}/videos")
        Call<MovieDBResult<Video>> listVideos(@Path("movie") int idMovie, @Query("api_key") String apiKey);
        
        @GET("movie/{movie}")
        Call<Movie> getDetails(@Path("movie") int idMovie, @Query("api_key") String apiKey);
    }
}
