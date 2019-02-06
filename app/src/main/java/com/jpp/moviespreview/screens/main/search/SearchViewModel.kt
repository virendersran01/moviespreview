package com.jpp.moviespreview.screens.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.jpp.mpdomain.SearchResult
import com.jpp.mpdomain.paging.MPPagingDataSourceFactory
import com.jpp.mpdomain.repository.SearchRepository
import com.jpp.mpdomain.usecase.search.ConfigSearchResultUseCase
import com.jpp.mpdomain.usecase.search.SearchUseCase
import com.jpp.mpdomain.usecase.search.SearchUseCaseResult
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * [ViewModel] to provide searchPage functionality.
 *
 * This is a very special ViewModel in the application, since it doesn't follows the pattern
 * defined in MPScopedViewModel. This is because this section of the application is using the
 * Paging Library to support unlimited scrolling and the [SearchRepository] is using its own
 * Executor to defer long term operations to another thread (instead of coroutines provided by
 * the MPScopedViewModel).
 */
class SearchViewModel @Inject constructor(private val searchUseCase: SearchUseCase,
                                          private val configSearchResultUseCase: ConfigSearchResultUseCase,
                                          private val networkExecutor: Executor) : ViewModel() {


    private var targetImageSize: Int = -1
    private val viewState = MediatorLiveData<SearchViewState>()

    fun init(imageSize: Int) {
        targetImageSize = imageSize
    }

    fun viewState(): LiveData<SearchViewState> = viewState

    fun search(searchText: String): LiveData<PagedList<SearchResultItem>> {
        return createSearchPagedList(searchText)
    }

    fun searchListUpdated(count: Int) {
        when (count) {
            0 -> SearchViewState.ErrorUnknown
            else -> SearchViewState.DoneSearching
        }
    }

    fun clearSearch() {

    }

    fun retryLastSearch() {

    }


    private fun createSearchPagedList(query: String): LiveData<PagedList<SearchResultItem>> {
        return MPPagingDataSourceFactory<SearchResult> { page, callback ->
            searchMoviePage(query, page, callback)
        }
                .map { configSearchResultUseCase.configure(targetImageSize, it) }
                .map { mapSearchResult(it) }
                .let {
                    // build the PagedList
                    val config = PagedList.Config.Builder()
                            .setPrefetchDistance(3)
                            .build()
                    LivePagedListBuilder(it, config)
                            .setFetchExecutor(networkExecutor)
                            .build()
                }
    }


    private fun searchMoviePage(queryString: String, page: Int, callback: (List<SearchResult>, Int) -> Unit) {
        if (page == 1) {
            viewState.postValue(SearchViewState.Searching)
        }
        searchUseCase
                .search(queryString, page)
                .let { ucResult ->
                    when (ucResult) {
                        is SearchUseCaseResult.ErrorNoConnectivity -> viewState.postValue(SearchViewState.ErrorNoConnectivity)
                        is SearchUseCaseResult.ErrorUnknown -> viewState.postValue(SearchViewState.ErrorUnknown)
                        is SearchUseCaseResult.Success -> callback(ucResult.result.results, page + 1)
                    }
                }
    }

    private fun mapSearchResult(searchResult: SearchResult) = with(searchResult) {
        with(searchResult) {
            SearchResultItem(
                    id = id,
                    imagePath = extractImagePathFromSearchResult(this),
                    name = extractTitleFromSearchResult(this),
                    icon = getIconForSearchResult(this)
            )
        }
    }

    private fun extractImagePathFromSearchResult(domainSearchResult: SearchResult) = when (domainSearchResult.isMovie()) {
        true -> domainSearchResult.poster_path ?: "Unknown"
        else -> domainSearchResult.profile_path ?: "Unknown" // case: TV and MOVIE
    }

    private fun extractTitleFromSearchResult(domainSearchResult: SearchResult) = when (domainSearchResult.isMovie()) {
        true -> domainSearchResult.title ?: "Unknown"
        else -> domainSearchResult.name ?: "Unknown" // case: TV and PERSON
    }

    private fun getIconForSearchResult(domainSearchResult: SearchResult) = when (domainSearchResult.isMovie()) {
        true -> SearchResultTypeIcon.MovieType
        else -> SearchResultTypeIcon.PersonType
    }
}