package com.github.ovictorpinto.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.popularmovies.model.Movie;
import com.github.ovictorpinto.popularmovies.model.Review;
import com.github.ovictorpinto.popularmovies.model.Video;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by victorpinto on 19/04/17. 
 */

public class DetailRecyclerAdapter extends RecyclerView.Adapter<DetailRecyclerAdapter.ViewHolder> {
    
    interface VideoClickListener {
        void onClick(Video video);
    }
    
    private static final int HEADER = 0;
    private static final int ITEM_HEADER = 1;
    private static final int VIDEO = 2;
    private static final int REVIEW = 3;
    
    private List<Item> list;
    private Context context;
    private Movie movie;
    private VideoClickListener movieClickListener;
    
    class Item {
        String title;
        Video video;
        Review review;
        
        public Item(String title) {
            this.title = title;
        }
        
        public Item(Video video) {
            this.video = video;
        }
        
        public Item(Review review) {
            this.review = review;
        }
    }
    
    public DetailRecyclerAdapter(Context context, List<Video> videos, List<Review> reviews, Movie movie, VideoClickListener
            movieClickListener) {
        this.context = context;
        this.movie = movie;
        this.movieClickListener = movieClickListener;
        
        list = new ArrayList<>();
        if (videos != null && !videos.isEmpty()) {
            list.add(new Item(context.getString(R.string.videos)));
            for (Video video : videos) {
                list.add(new Item(video));
            }
        }
        if (reviews != null && !reviews.isEmpty()) {
            list.add(new Item(context.getString(R.string.reviews)));
            for (Review review : reviews) {
                list.add(new Item(review));
            }
        }
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout;
        switch (viewType) {
            case HEADER:
                layout = R.layout.detail_header;
                break;
            case ITEM_HEADER:
                layout = R.layout.ly_item_header;
                break;
            case VIDEO:
                layout = R.layout.ly_item_video;
                break;
            default:
            case REVIEW:
                layout = R.layout.ly_item_review;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            //fillHeader
            String vote = " - ";
            if (movie.getVote_average() != null) {
                vote = movie.getVote_average().toString();
            }
            holder.textviewRating.setText(vote);
            
            String date = " - ";
            if (movie.getRelease() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(movie.getRelease());
                date = String.valueOf(calendar.get(Calendar.YEAR));
            }
            holder.textviewYear.setText(date);
            holder.textviewContent.setText(movie.getOverview());
            holder.textviewTime.setText(context.getString(R.string.time_, movie.getRuntime()));
            holder.textviewTitle.setText(movie.getTitle());
            
            Picasso.with(context).load(movie.getFullPath()).into(holder.imageView);
        } else {
            final Item item = list.get(position - 1);
            switch (getItemViewType(position)) {
                case ITEM_HEADER:
                    holder.textviewTitle.setText(item.title);
                    break;
                case VIDEO:
                    holder.textviewTitle.setText(item.video.getName());
                    if (movieClickListener != null) {
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                movieClickListener.onClick(item.video);
                            }
                        });
                    }
                    break;
                case REVIEW:
                    holder.textviewAuthor.setText(item.review.getAuthor());
                    holder.textviewContent.setText(item.review.getContent());
                    break;
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        }
        Item item = list.get(position - 1);
        if (item.title != null) {
            return ITEM_HEADER;
        }
        if (item.video != null) {
            return VIDEO;
        }
        return REVIEW;
    }
    
    @Override
    public int getItemCount() {
        return list.size() + 1;//detlahe do filme sempre existe
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        
        TextView textviewTitle;
        TextView textviewAuthor;
        TextView textviewContent;
        
        TextView textviewYear;
        TextView textviewTime;
        TextView textviewRating;
        
        ImageView imageView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            
            textviewTitle = (TextView) itemView.findViewById(R.id.textview);
            textviewAuthor = (TextView) itemView.findViewById(R.id.textview_author);
            textviewContent = (TextView) itemView.findViewById(R.id.textview_content);
            
            textviewYear = (TextView) itemView.findViewById(R.id.textview_date);
            textviewTime = (TextView) itemView.findViewById(R.id.textview_time);
            textviewRating = (TextView) itemView.findViewById(R.id.textview_vote);
            
            imageView = (ImageView) itemView.findViewById(R.id.imageview);
        }
    }
}
