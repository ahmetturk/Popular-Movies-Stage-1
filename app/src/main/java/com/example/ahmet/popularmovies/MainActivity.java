package com.example.ahmet.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.ahmet.popularmovies.models.Movie;

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

    private static final int SPAN_COUNT = 2;

    private MovieAdapter mMoviesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.movies_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager);

        mMoviesAdapter = new MovieAdapter(this, this);
        recyclerView.setAdapter(mMoviesAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String sortMethod = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute(sortMethod);
    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("moviePoster", movie.getMoviePoster());
        intent.putExtra("movieTitle", movie.getMovieTitle());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("userRating", movie.getUserRating());
        intent.putExtra("plotSynopsis", movie.getPlotSynopsis());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popular_movies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // It will contain the raw JSON response as a string
            String moviesJsonStr = null;

            try {
                // params[0] is the sort method of movies
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=" + params[0] + "&api_key=" + API_KEY);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0)
                {
                    return null;
                }

                moviesJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> moviesList) {
            super.onPostExecute(moviesList);
            mMoviesAdapter.setMoviesList(moviesList);
        }

        private List<Movie> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
            List<Movie> moviesList = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(moviesJsonStr);
            JSONArray movies =  jsonObject.getJSONArray("results");
            for(int i = 0; i < movies.length(); i++) {
                JSONObject movieDetail = movies.getJSONObject(i);
                // 0 is image url of the poster of movie
                // 1 is original name of movie
                // 2 is release date of movie
                // 3 is user rating of movie
                // 4 is plot synopsis of movie
                String imageUrl = "http://image.tmdb.org/t/p/w185/" + movieDetail.getString("poster_path");
                String movieName = movieDetail.getString("original_title");
                String releaseDate = movieDetail.getString("release_date");
                String userRating = movieDetail.getString("vote_average") + "/10";
                String plotSynopsis = movieDetail.getString("overview");

                moviesList.add(new Movie(imageUrl, movieName, releaseDate, userRating, plotSynopsis));
            }
            return moviesList;
        }
    }
}
