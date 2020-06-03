package com.jpp.mpmoviedetails

import android.view.View
import androidx.lifecycle.SavedStateHandle
import com.jpp.mp.common.navigation.Destination
import com.jpp.mpdomain.MovieDetail
import com.jpp.mpdomain.MovieGenre
import com.jpp.mpdomain.usecase.GetMovieDetailUseCase
import com.jpp.mpdomain.usecase.Try
import com.jpp.mptestutils.CoroutineTestExtension
import com.jpp.mptestutils.InstantTaskExecutorExtension
import com.jpp.mptestutils.observeWith
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(
    MockKExtension::class,
    InstantTaskExecutorExtension::class,
    CoroutineTestExtension::class
)
class MovieDetailsViewModelTest {

    @RelaxedMockK
    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCase

    private val savedStateHandle: SavedStateHandle by lazy {
        SavedStateHandle()
    }

    private lateinit var subject: MovieDetailsViewModel

    @BeforeEach
    fun setUp() {
        subject = MovieDetailsViewModel(
            getMovieDetailUseCase,
            CoroutineTestExtension.testDispatcher,
            savedStateHandle
        )
    }

    @Test
    fun `Should post no connectivity error when disconnected`() {
        var viewStatePosted: MovieDetailViewState? = null

        coEvery { getMovieDetailUseCase.execute(any()) } returns Try.Failure(Try.FailureCause.NoConnectivity)

        subject.viewState.observeWith { viewState -> viewStatePosted = viewState }
        subject.onInit(MovieDetailsParam(10.0, "aMovie", "aUrl"))

        assertNotNull(viewStatePosted)
        assertEquals(View.INVISIBLE, viewStatePosted?.loadingVisibility)
        assertEquals(View.INVISIBLE, viewStatePosted?.contentViewState?.visibility)

        assertEquals(View.VISIBLE, viewStatePosted?.errorViewState?.visibility)
        assertEquals(true, viewStatePosted?.errorViewState?.isConnectivity)
    }

    @Test
    fun `Should post error when failing to fetch user account data`() {
        var viewStatePosted: MovieDetailViewState? = null

        coEvery { getMovieDetailUseCase.execute(any()) } returns Try.Failure(Try.FailureCause.Unknown)

        subject.viewState.observeWith { viewState -> viewStatePosted = viewState }
        subject.onInit(MovieDetailsParam(10.0, "aMovie", "aUrl"))

        assertNotNull(viewStatePosted)
        assertEquals(View.INVISIBLE, viewStatePosted?.loadingVisibility)
        assertEquals(View.INVISIBLE, viewStatePosted?.contentViewState?.visibility)

        assertEquals(View.VISIBLE, viewStatePosted?.errorViewState?.visibility)
        assertEquals(false, viewStatePosted?.errorViewState?.isConnectivity)
    }

    @Test
    fun `Should execute GetMovieDetailsUseCase, adapt result to UI model and post value on init`() {
        var viewStatePosted: MovieDetailViewState? = null
        val movieDetailId = 12.0

        val domainDetail = MovieDetail(
            id = movieDetailId,
            title = "aTitle",
            overview = "anOverview",
            release_date = "aReleaseDate",
            vote_count = 12.toDouble(),
            vote_average = 15F,
            popularity = 178F,
            poster_path = null,
            genres = listOf(
                MovieGenre(28, "Action"),
                MovieGenre(27, "Horror")
            )
        )

        val expected = MovieDetailViewState.showLoading("aUrl").showDetails(
            movieImageUrl = "aUrl",
            overview = domainDetail.overview,
            releaseDate = domainDetail.release_date,
            voteCount = domainDetail.vote_count.toString(),
            popularity = domainDetail.popularity.toString(),
            genres = listOf(
                MovieGenreItem.Action,
                MovieGenreItem.Horror
            )
        )

        coEvery { getMovieDetailUseCase.execute(movieDetailId) } returns Try.Success(domainDetail)

        subject.viewState.observeWith { viewState -> viewStatePosted = viewState }
        subject.onInit(MovieDetailsParam(movieDetailId, "aMovie", "aUrl"))

        assertEquals(expected, viewStatePosted)
    }

    @Test
    fun `Should update reached destination in onInit`() {
        var destinationReached: Destination? = null
        val expected = Destination.ReachedDestination("aMovie")

        subject.destinationEvents.observeWith { destinationReached = it }

        subject.onInit(MovieDetailsParam(10.0, "aMovie", "aUrl"))

        assertEquals(expected, destinationReached)
    }
}
