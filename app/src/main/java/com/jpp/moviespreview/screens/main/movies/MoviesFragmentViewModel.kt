package com.jpp.moviespreview.screens.main.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.jpp.mpdomain.Movie
import com.jpp.mpdomain.MovieSection
import com.jpp.mpdomain.repository.RepositoryState
import com.jpp.mpdomain.repository.movies.MovieListRepository

/**
 * [ViewModel] to support the movies list section in the application.
 *
 * This is a very special ViewModel in the application, since it doesn't follows the pattern
 * defined in MPScopedViewModel. This is because this section of the application is using the
 * Paging Library to support unlimited scrolling and that library requires that the DataSource
 * behaves more as a controller in the architecture defined in MoviesPreview.
 */
abstract class MoviesFragmentViewModel(private val movieListRepository: MovieListRepository,
                                       private val movieSection: MovieSection) : ViewModel() {


    lateinit var viewState: LiveData<MoviesFragmentViewState>
    lateinit var pagedList: LiveData<PagedList<MovieItem>>
    private lateinit var retryFun: () -> Unit


    fun init(moviePosterSize: Int,
             movieBackdropSize: Int) {

        movieListRepository.moviePageForSection(movieSection, movieBackdropSize, moviePosterSize) { domainMovie ->
            mapDomainMovie(domainMovie)
        }.let { listing ->
            /*
             * Very cool way to map the ds internal state to a state that the UI understands without coupling the UI to de domain.
             */
            viewState = map(listing.operationState) {
                when (it) {
                    RepositoryState.Loading -> MoviesFragmentViewState.Loading
                    is RepositoryState.ErrorUnknown -> {
                        when (it.hasItems) {
                            true -> MoviesFragmentViewState.ErrorUnknownWithItems
                            false -> MoviesFragmentViewState.ErrorUnknown
                        }
                    }
                    is RepositoryState.ErrorNoConnectivity -> {
                        when (it.hasItems) {
                            true -> MoviesFragmentViewState.ErrorNoConnectivityWithItems
                            false -> MoviesFragmentViewState.ErrorNoConnectivity
                        }
                    }
                    RepositoryState.Loaded -> MoviesFragmentViewState.InitialPageLoaded
                    else -> MoviesFragmentViewState.None
                }
            }

            pagedList = listing.pagedList
            retryFun = listing.retry
        }
    }


    fun retryMoviesListFetch() {
        retryFun.invoke()
    }


    private fun mapDomainMovie(domainMovie: Movie) = with(domainMovie) {
        MovieItem(movieId = id,
                headerImageUrl = backdrop_path ?: "emptyPath",
                title = title,
                contentImageUrl = poster_path ?: "emptyPath",
                popularity = popularity.toString(),
                voteCount = vote_count.toString()
        )
    }
}