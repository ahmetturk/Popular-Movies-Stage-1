package com.example.ahmet.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String movieTitle = intent.getStringExtra("movieTitle");
        String moviePoster = intent.getStringExtra("moviePoster");
        String releaseDate = intent.getStringExtra("releaseDate");
        String userRating = intent.getStringExtra("userRating");
        String plotSynopsis = intent.getStringExtra("plotSynopsis");
        ImageView moviePosterImageView = findViewById(R.id.movie_poster);
        Picasso.with(this).load(moviePoster).into(moviePosterImageView);
        TextView movieTitleTextView = findViewById(R.id.movie_title);
        movieTitleTextView.setText(movieTitle);
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
            releaseDate = DateFormat.getDateInstance().format(date);
        }
        catch (ParseException e) {
            Log.e("DetailActivity", e.getMessage(), e);
        }
        TextView releaseDateTextView = findViewById(R.id.release_date);
        releaseDateTextView.setText(releaseDate);
        TextView userRatingTextView = findViewById(R.id.user_rating);
        userRatingTextView.setText(userRating);
        TextView plotSynopsisTextView = findViewById(R.id.plot_synopsis);
        plotSynopsisTextView.setText(plotSynopsis);

    }
}
