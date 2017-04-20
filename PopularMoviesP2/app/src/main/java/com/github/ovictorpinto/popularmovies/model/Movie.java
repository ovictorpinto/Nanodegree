package com.github.ovictorpinto.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by victor on 18/10/16.
 */

public class Movie implements Parcelable {
    
    public static final String PARAM = "movieParam";
    public static final String PARAM_ID = "idMovieParam";
    public static final String URL_PREFIX = "http://image.tmdb.org/t/p/w185";
    
    protected int id;
    @JsonProperty(value = "poster_path")
    protected String posterPath;
    protected String overview;
    protected String title;
    @JsonProperty(value = "release_date")
    protected Date release;
    protected Float vote_average;
    protected int runtime;
    protected boolean isFavorito;
    
    public Movie() {
    }
    
    public String getFullPath() {
        return URL_PREFIX + posterPath;
    }
    
    protected Movie(Parcel in) {
        posterPath = in.readString();
        overview = in.readString();
        title = in.readString();
        long releaseLong = in.readLong();
        if (releaseLong > 0) {
            release = new Date(releaseLong);
        }
        vote_average = (Float) in.readSerializable();
        id = in.readInt();
        runtime = in.readInt();
        isFavorito = in.readInt() > 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(title);
        dest.writeLong(release != null ? release.getTime() : 0);
        dest.writeSerializable(vote_average);
        dest.writeInt(id);
        dest.writeInt(runtime);
        dest.writeInt(isFavorito ? 1 : 0);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }
        
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    
    public String getPosterPath() {
        return posterPath;
    }
    
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    
    public String getOverview() {
        return overview;
    }
    
    public void setOverview(String overview) {
        this.overview = overview;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Date getRelease() {
        return release;
    }
    
    public void setRelease(Date release) {
        this.release = release;
    }
    
    public Float getVote_average() {
        return vote_average;
    }
    
    public void setVote_average(Float vote_average) {
        this.vote_average = vote_average;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getRuntime() {
        return runtime;
    }
    
    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }
    
    public boolean isFavorito() {
        return isFavorito;
    }
    
    public void setFavorito(boolean favorito) {
        isFavorito = favorito;
    }
}
