package com.github.ovictorpinto.popularmovies.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by victorpinto on 19/04/17. 
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDBResult<T> {
    List<T> results;
    
    public List<T> getResults() {
        return results;
    }
    
    public void setResults(List<T> results) {
        this.results = results;
    }
}
