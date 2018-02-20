package com.example.ahmet.popularmovies.models;

public class Movie {
    private String movieTitle;
    private String moviePoster;
    private String plotSynopsis;
    private String userRating;
    private String releaseDate;

    public Movie(String moviePoster, String movieTitle, String releaseDate, String userRating, String plotSynopsis) {
        this.moviePoster = moviePoster;
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.plotSynopsis = plotSynopsis;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        this.moviePoster = moviePoster;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }
}
