package com.jpp.mp.main.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.jpp.mp.main.movies.MovieListInteractor.MovieListEvent.NotConnectedToNetwork
import com.jpp.mp.main.movies.MovieListInteractor.MovieListEvent.UnknownError
import com.jpp.mp.main.movies.MovieListInteractor.MovieListEvent.UserChangedLanguage
import com.jpp.mpdomain.Connectivity
import com.jpp.mpdomain.Movie
import com.jpp.mpdomain.MovieSection
import com.jpp.mpdomain.repository.ConnectivityRepository
import com.jpp.mpdomain.repository.LanguageRepository
import com.jpp.mpdomain.repository.MoviePageRepository
import javax.inject.Inject

/**
 * Interactor to support the list of movies that can be seeing in the home screen (Playing,
 * Popular, TopRated and Incoming).
 * Since this interactor is used with the paging library, it provides two ways to communicate the
 * responses obtained from the data layer:
 *  - One way, is a [LiveData] object that publishes status events obtained in error
 *    scenarios ([UserMovieListEvent]).
 *  - The other way is using a callback. This is defined this way because of the limitation
 *    in the paging library, where a callback needs to be provided - instead of using
 *    a reactive approach.
 */
class MovieListInteractor @Inject constructor(
    private val moviePageRepository: MoviePageRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val languageRepository: LanguageRepository
) {

    /**
     * Represents all the events that can be posted by the interactor as response
     * to an action executed. Subscribe to [events] to get notified
     * when clients needs to react to an event generated by the interactor.
     */
    sealed class MovieListEvent {
        object UserChangedLanguage : MovieListEvent()
        object NotConnectedToNetwork : MovieListEvent()
        object UnknownError : MovieListEvent()
    }

    private val _movieListEvents = MediatorLiveData<MovieListEvent>()
    val events: LiveData<MovieListEvent> get() = _movieListEvents

    init {
        _movieListEvents.addSource(languageRepository.updates()) { _movieListEvents.postValue(UserChangedLanguage) }
    }

    /**
     * Fetches a page of movies (indicated by [page]) that corresponds to the movies section
     * from the repository. If the page can be successfully retrieved,
     * the list of [Movie]s that corresponds to the page will be posted to [callback].
     * If an error is detected, then the proper event will be posted to [events].
     *
     * IMPORTANT: this method takes a [callback] as input in order to provide support to
     * the Android Paging Library. This is why this interactor has basically two points
     * of outputs: [events] and the provided [callback].
     */
    fun fetchMoviePageForSection(page: Int, section: MovieSection, callback: (List<Movie>) -> Unit) {
        when (connectivityRepository.getCurrentConnectivity()) {
            is Connectivity.Disconnected -> _movieListEvents.postValue(NotConnectedToNetwork)
            is Connectivity.Connected ->
                moviePageRepository.getMoviePageForSection(
                        page,
                        section,
                        languageRepository.getCurrentAppLanguage()
                )?.let { callback(it.results) } ?: _movieListEvents.postValue(UnknownError)
        }
    }

    /**
     * Flushes out all data related to the section provided.
     */
    fun flushMoviePagesForSection(section: MovieSection) {
        moviePageRepository.flushMoviePagesForSection(section)
    }
}
