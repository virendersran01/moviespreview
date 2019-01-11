package com.jpp.mpdomain.repository.movies

import com.jpp.mpdomain.MoviePage

/**
 * API definition to retrieve all movies related data from the server.
 */
interface MoviesApi {
    /**
     * @return the [MoviePage] that contains the movies being played right now.
     * Null if no data is available.
     */
    fun getNowPlayingMoviePage(page: Int): MoviePage?
    /**
     * @return the [MoviePage] that contains the most popular movies.
     * Null if no data is available.
     */
    fun getPopularMoviePage(page: Int): MoviePage?
    /**
     * @return the [MoviePage] that contains the top rated movies.
     * Null if no data is available.
     */
    fun getTopRatedMoviePage(page: Int): MoviePage?
    /**
     * @return the [MoviePage] that contains the upcoming movies.
     * Null if no data is available.
     */
    fun getUpcomingMoviePage(page: Int): MoviePage?
}