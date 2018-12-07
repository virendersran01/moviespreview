package com.jpp.moviespreview.screens.main.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jpp.moviespreview.domainlayer.interactor.ConfigureApplicationResult
import com.jpp.moviespreview.domainlayer.interactor.ConfigureApplicationUseCase
import com.jpp.moviespreview.screens.MPScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoviesFragmentViewModel @Inject constructor(private val configAppUseCase: ConfigureApplicationUseCase) : MPScopedViewModel() {

    private val viewState by lazy { MutableLiveData<MoviesFragmentViewState>().apply { value = MoviesFragmentViewState.Loading } }

    fun bindViewState(): LiveData<MoviesFragmentViewState> = viewState

    fun onIntent(intent: MoviesFragmentIntent) {
        launch {
            when (intent) {
                MoviesFragmentIntent.ConfigureApplication -> viewState.value = withContext(Dispatchers.Default) { configure() }
            }
        }
    }

    private fun configure(): MoviesFragmentViewState {
        return when (configAppUseCase()) {
            ConfigureApplicationResult.ErrorNoConnectivity -> MoviesFragmentViewState.ErrorNoConnectivity
            ConfigureApplicationResult.ErrorUnknown -> MoviesFragmentViewState.ErrorUnknown
            ConfigureApplicationResult.Success -> MoviesFragmentViewState.Configured
        }
    }
}