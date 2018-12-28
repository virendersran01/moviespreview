package com.jpp.moviespreview.datalayer.cache


import com.jpp.moviespreview.domainlayer.AppConfiguration
import com.jpp.moviespreview.domainlayer.MovieDetail
import com.jpp.moviespreview.domainlayer.MoviePage

interface MPDataBase {
    fun getStoredAppConfiguration(): AppConfiguration?
    fun updateAppConfiguration(appConfiguration: AppConfiguration)
    fun isCurrentMovieTypeStored(movieType: MovieType): Boolean
    fun updateCurrentMovieTypeStored(movieType: MovieType)
    fun getMoviePage(page: Int): MoviePage?
    fun updateMoviePage(page: MoviePage)
    fun clearMoviePagesStored()
    fun getMovieDetail(movieDetailId: Double): MovieDetail?
    fun cleanMovieDetail(movieDetailId: Double)
    fun saveMovieDetail(movieDetail: MovieDetail)
}
