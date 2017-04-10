package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private BroadcastReceiver onConnectReceiver;
    private Snackbar snack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);
        
        if (savedInstanceState == null) {
            refresh();
        }
    }
    
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            return false;
        }
        return true;
    }
    
    private void refresh() {
        if (!isOnline()) {
            snack = Snackbar.make(findViewById(R.id.main_layout), R.string.offline, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    refresh();
                                }
                            });
            snack.show();
            waitConnection();
        } else {
            startService(new Intent(this, UpdaterService.class));
        }
        
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }
    
    private boolean mIsRefreshing = false;
    
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };
    
    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }
    
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }
        
        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = vh.getAdapterPosition();
                    
                    String transitionName = ViewCompat.getTransitionName(vh.thumbnailView);
                    Log.d("adapter", transitionName);
                    Bundle bundle = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(ArticleListActivity.this, vh.thumbnailView, transitionName).toBundle();
                    Uri uri = ItemsContract.Items.buildItemUri(getItemId(position));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.putExtra("pos", position);
                    startActivity(intent, bundle);
                }
            });
            return vh;
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            ViewCompat.setTransitionName(holder.thumbnailView, getString(R.string.transition_photo) + mCursor
                    .getString(ArticleLoader.Query._ID));
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.subtitleView.setText(DateUtils.getRelativeTimeSpanString(mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE), System
                    .currentTimeMillis(), DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString() + " by " + mCursor
                    .getString(ArticleLoader.Query.AUTHOR));
            
            holder.thumbnailView
                    .setImageUrl(mCursor.getString(ArticleLoader.Query.THUMB_URL), ImageLoaderHelper.getInstance(ArticleListActivity.this)
                                                                                                    .getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }
        
        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
        
        public ViewHolder(View view) {
            super(view);
            
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
        
    }
    
    private void waitConnection() {
        if (onConnectReceiver == null) {
            onConnectReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isOnline()) {
                        refresh();
                        unregisterReceiver(onConnectReceiver);
                        onConnectReceiver = null;
                        if (snack.isShown()) {
                            snack.dismiss();
                        }
                    }
                }
            };
            registerReceiver(onConnectReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onConnectReceiver != null) {
            unregisterReceiver(onConnectReceiver);
        }
    }
}
