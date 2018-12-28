package com.jpp.moviespreview.datalayer.cache.repository


import com.jpp.moviespreview.datalayer.cache.MPDataBase
import com.jpp.moviespreview.datalayer.cache.MovieType
import com.jpp.moviespreview.datalayer.cache.timestamp.MPTimestamps
import com.jpp.moviespreview.domainlayer.MovieDetail
import com.jpp.moviespreview.domainlayer.MoviePage
import com.jpp.moviespreview.domainlayer.repository.MoviesRepository

/**
 * [MoviesRepository] implementation with cache functionality.
 */
class CacheMoviesRepository(private val mpCache: MPTimestamps,
                            private val mpDatabase: MPDataBase) : MoviesRepository {


    override fun getNowPlayingMoviePage(page: Int): MoviesRepository.MoviesRepositoryOutput = getMoviePageOrClearDataBaseIfNeeded(MovieType.NowPlaying, page)

    override fun getPopularMoviePage(page: Int): MoviesRepository.MoviesRepositoryOutput = getMoviePageOrClearDataBaseIfNeeded(MovieType.Popular, page)

    override fun getTopRatedMoviePage(page: Int): MoviesRepository.MoviesRepositoryOutput = getMoviePageOrClearDataBaseIfNeeded(MovieType.TopRated, page)

    override fun getUpcomingMoviePage(page: Int): MoviesRepository.MoviesRepositoryOutput = getMoviePageOrClearDataBaseIfNeeded(MovieType.Upcoming, page)

    override fun getMovieDetail(movieId: Double): MoviesRepository.MoviesRepositoryOutput {
        return when (mpCache.isMovieDetailUpToDate(movieId)) {
            true -> mpDatabase.getMovieDetail(movieId)
                    ?.let { MoviesRepository.MoviesRepositoryOutput.MovieDetailsRetrieved(it) }
                    ?: let { MoviesRepository.MoviesRepositoryOutput.Error }
            false -> {
                mpDatabase.cleanMovieDetail(movieId)
                MoviesRepository.MoviesRepositoryOutput.Error
            }
        }
    }

    override fun updateNowPlayingMoviePage(moviePage: MoviePage) = updateMoviePage(MovieType.NowPlaying, moviePage)

    override fun updatePopularMoviePage(moviePage: MoviePage) = updateMoviePage(MovieType.Popular, moviePage)

    override fun updateTopRatedMoviePage(moviePage: MoviePage) = updateMoviePage(MovieType.TopRated, moviePage)

    override fun updateUpcomingMoviePage(moviePage: MoviePage) = updateMoviePage(MovieType.Upcoming, moviePage)

    override fun updateMovieDetail(movieDetail: MovieDetail) {
        with(mpDatabase) {
            saveMovieDetail(movieDetail)
        }
    }

    /**
     * Updates the [MoviePage] in the local storage and the timestamp for when the movie page
     * has been inserted.
     */
    private fun updateMoviePage(movieType: MovieType, moviePage: MoviePage) {
        with(mpDatabase) {
            updateCurrentMovieTypeStored(movieType)
            updateMoviePage(moviePage)
        }.also {
            mpCache.updateMoviesInserted()
        }
    }

    /**
     * Verifies if the data stored in the database is valid (based on the timestamp) and retrieves it
     * if it is. If it is not valid, it clears the local storage in order to keep the database clean.
     */
    private fun getMoviePageOrClearDataBaseIfNeeded(movieType: MovieType, page: Int): MoviesRepository.MoviesRepositoryOutput {
        return when (shouldRetrieveMoviePage(movieType)) {
            true -> mpDatabase.getMoviePage(page)
                    ?.let { MoviesRepository.MoviesRepositoryOutput.MoviePageRetrieved(it) }
                    ?: let { MoviesRepository.MoviesRepositoryOutput.Error }
            else -> {
                mpDatabase.clearMoviePagesStored()
                MoviesRepository.MoviesRepositoryOutput.Error
            }
        }
    }

    private fun shouldRetrieveMoviePage(movieType: MovieType): Boolean =
        mpDatabase.isCurrentMovieTypeStored(movieType) && mpCache.areMoviesUpToDate()
}