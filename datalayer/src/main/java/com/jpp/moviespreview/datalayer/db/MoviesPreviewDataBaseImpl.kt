package com.jpp.moviespreview.datalayer.db

import android.content.Context
import androidx.room.Room
import com.jpp.moviespreview.datalayer.AppConfiguration
import com.jpp.moviespreview.datalayer.MoviePage
import com.jpp.moviespreview.datalayer.db.room.MPRoomDataBase
import com.jpp.moviespreview.datalayer.db.room.RoomModelAdapter

class MoviesPreviewDataBaseImpl(private val context: Context,
                                private val adapter: RoomModelAdapter) : MoviesPreviewDataBase {


    private val roomDatabase by lazy {
        Room.databaseBuilder(context, MPRoomDataBase::class.java, "MPRoomDataBase")
                .build()
    }

    override fun getStoredAppConfiguration(): AppConfiguration? =
        roomDatabase
                .imageSizeDao()
                .getImageSizes()?.let {
                    adapter.adaptImageSizesToAppConfiguration(it)
                }

    override fun updateAppConfiguration(appConfiguration: AppConfiguration) {
        adapter.adaptAppConfigurationToImageSizes(appConfiguration)
                .let {
                    roomDatabase
                            .imageSizeDao()
                            .insertImageSizes(it)
                }
    }


    override fun isCurrentMovieTypeStored(movieType: MovieType): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCurrentMovieTypeStored(movieType: MovieType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMoviePage(page: Int): MoviePage? =
        roomDatabase
                .moviePageDao()
                .getMoviePage(page)?.let { moviePage ->
                    roomDatabase
                            .moviesDao()
                            .getMoviesFromPage(moviePage.page)?.let {
                                adapter.adaptDBMoviePageToDataMoviePage(moviePage, it)
                            }
                }


    override fun updateMoviePage(page: MoviePage) {
        adapter.adaptDataMoviePageToDBMoviePage(page)
                .let {
                    roomDatabase
                            .moviePageDao()
                            .insertMoviePage(it)
                }
        page.results
                .map { movie -> adapter.adaptDataMovieToDBMovie(movie, page.page) }
                .forEach { dbMovie -> roomDatabase.moviesDao().insertMovie(dbMovie) }
    }

    override fun clearMoviePagesStored() {
        with(roomDatabase) {
            moviesDao().deleteAllMovies()
            moviePageDao().deleteAllPages()
        }
    }
}