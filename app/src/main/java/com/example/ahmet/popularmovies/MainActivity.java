package com.example.ahmet.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ahmet.popularmovies.models.Movie;
import com.example.ahmet.popularmovies.utils.GridItemDecoration;
import com.example.ahmet.popularmovies.utils.RecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.ahmet.popularmovies.BuildConfig.API_KEY;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    // Grid Layout Horizontal Item Count
    private static final int SPAN_COUNT = 2;

    // JSON Keys
    private static final String MOVIE_TITLE_KEY = "title";
    private static final String POSTER_PATH_KEY = "poster_path";
    private static final String PLOT_SYNOPSIS_KEY = "overview";
    private static final String USER_RATING_KEY = "vote_average";
    private static final String RELEASE_DATE_KEY = "release_date";
    private static final String BACKDROP_PATH_KEY = "backdrop_path";

    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView internetStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetStatusTv = findViewById(R.id.internet_status);
        RecyclerView recyclerView = findViewById(R.id.movies_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addItemDecoration(new GridItemDecoration(this));

        mMoviesAdapter = new MovieAdapter(this, this);
        recyclerView.setAdapter(mMoviesAdapter);

        mScrollListener = new RecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                fetchNewMovies(page);
            }
        };

        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateUI();
            }
        });
    }

    private void populateUI() {
        mScrollListener.resetState();
        mMoviesAdapter.clearMoviesList();
        fetchNewMovies(1);
    }

    private void fetchNewMovies(int page) {
        int sorting = PopularMoviesPreferences.getSorting(this);
        String sortMethod = getResources().getStringArray(R.array.sort_pref_list)[sorting];

        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute(sortMethod, String.valueOf(page));
    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popular_movies, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(PopularMoviesPreferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PopularMoviesPreferences.setSorting(MainActivity.this, i);
                populateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return true;
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isOnline()) {
                internetStatusTv.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            try {
                String language = getString(R.string.language);

                Uri uri = Uri.parse("https://api.themoviedb.org/3/movie/").buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter("language", language)
                        .appendQueryParameter("page", params[1])
                        .build();
                URL url = new URL(uri.toString());

                String jsonMoviesResponse = getResponseFromHttpUrl(url);

                return getMoviesDataFromJson(jsonMoviesResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> moviesList) {
            super.onPostExecute(moviesList);
            mSwipeRefreshLayout.setRefreshing(false);
            if (moviesList != null) {
                internetStatusTv.setVisibility(View.GONE);
                mMoviesAdapter.addMoviesList(moviesList);
            }
        }

        private String getResponseFromHttpUrl(URL url) throws IOException {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    return null;
                }

                return builder.toString();
            } finally {
                urlConnection.disconnect();
            }
        }

        private List<Movie> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
            List<Movie> moviesList = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(moviesJsonStr);
            JSONArray movies = jsonObject.getJSONArray("results");
            for (int i = 0; i < movies.length(); i++) {
                JSONObject movieDetail = movies.getJSONObject(i);

                /* movieName is original name of movie
                 * posterPath is image url of the poster of movie
                 * plotSynopsis is plot synopsis of movie
                 * userRating is user rating of movie
                 * releaseDate is release date of movie
                 * backdropPath is image url of the backdrop of movie*/

                String movieName = movieDetail.getString(MOVIE_TITLE_KEY);
                String posterPath = "http://image.tmdb.org/t/p/w185/" + movieDetail.getString(POSTER_PATH_KEY);
                String plotSynopsis = movieDetail.getString(PLOT_SYNOPSIS_KEY);
                String userRating = movieDetail.getString(USER_RATING_KEY);
                String releaseDate = movieDetail.getString(RELEASE_DATE_KEY);
                String backdropPath = "http://image.tmdb.org/t/p/w300/" + movieDetail.getString(BACKDROP_PATH_KEY);

                moviesList.add(new Movie(movieName, posterPath, plotSynopsis, userRating, releaseDate, backdropPath));
            }
            return moviesList;
        }
    }
}
