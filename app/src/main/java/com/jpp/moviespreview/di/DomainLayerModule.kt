package com.jpp.moviespreview.di

import com.jpp.mpdomain.repository.*
import com.jpp.mpdomain.usecase.about.GetAboutNavigationUrlUseCase
import com.jpp.mpdomain.usecase.appversion.GetAppVersionUseCase
import com.jpp.mpdomain.usecase.credits.ConfigCastCharacterUseCase
import com.jpp.mpdomain.usecase.credits.GetCreditsUseCase
import com.jpp.mpdomain.usecase.details.GetMovieDetailsUseCase
import com.jpp.mpdomain.usecase.licenses.GetAppLicensesUseCase
import com.jpp.mpdomain.usecase.licenses.GetLicenseUseCase
import com.jpp.mpdomain.usecase.movies.ConfigMovieUseCase
import com.jpp.mpdomain.usecase.movies.GetMoviesUseCase
import com.jpp.mpdomain.usecase.person.GetPersonUseCase
import com.jpp.mpdomain.usecase.search.ConfigSearchResultUseCase
import com.jpp.mpdomain.usecase.search.SearchUseCase
import com.jpp.mpdomain.usecase.support.RefreshDataUseCase
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Provides all dependencies for the domain layer.
 */
@Module
class DomainLayerModule {

    @Provides
    @Singleton
    fun providesNetworkExecutor(): Executor = Executors.newFixedThreadPool(5)

    @Provides
    fun providesSearchUseCase(searchRepository: SearchRepository,
                              connectivityRepository: ConnectivityRepository)
            : SearchUseCase = SearchUseCase.Impl(searchRepository, connectivityRepository)

    @Provides
    @Singleton
    fun providesConfigSearchResultUseCase(configurationRepository: ConfigurationRepository)
            : ConfigSearchResultUseCase = ConfigSearchResultUseCase.Impl(configurationRepository)

    @Provides
    fun providesGetMoviesUseCase(moviesRepository: MoviesRepository,
                                 connectivityRepository: ConnectivityRepository,
                                 languageRepository: LanguageRepository)
            : GetMoviesUseCase = GetMoviesUseCase.Impl(moviesRepository, connectivityRepository, languageRepository)

    @Provides
    fun providesConfigMovieUseCase(configurationRepository: ConfigurationRepository)
            : ConfigMovieUseCase = ConfigMovieUseCase.Impl(configurationRepository)

    @Provides
    fun providesGetMovieDetailsUseCase(moviesRepository: MoviesRepository,
                                       connectivityRepository: ConnectivityRepository)
            : GetMovieDetailsUseCase = GetMovieDetailsUseCase.Impl(moviesRepository, connectivityRepository)

    @Provides
    fun providesGetPersonUseCase(personRepository: PersonRepository,
                                 connectivityRepository: ConnectivityRepository)
            : GetPersonUseCase = GetPersonUseCase.Impl(personRepository, connectivityRepository)

    @Provides
    fun providesGetCreditsUseCase(creditsRepository: CreditsRepository,
                                  connectivityRepository: ConnectivityRepository)
            : GetCreditsUseCase = GetCreditsUseCase.Impl(creditsRepository, connectivityRepository)

    @Provides
    fun providesConfigCastCharacterUseCase(configurationRepository: ConfigurationRepository)
            : ConfigCastCharacterUseCase = ConfigCastCharacterUseCase.Impl(configurationRepository)

    @Provides
    fun providesGetAppVersionUseCase(appVersionRepository: AppVersionRepository)
            : GetAppVersionUseCase = GetAppVersionUseCase.Impl(appVersionRepository)

    @Provides
    fun providesGetAboutNavigationUrlUseCase(aboutNavigationRepository: AboutNavigationRepository)
            : GetAboutNavigationUrlUseCase = GetAboutNavigationUrlUseCase.Impl(aboutNavigationRepository)

    @Provides
    fun providesGetAppLicensesUseCase(licensesRepository: LicensesRepository)
            : GetAppLicensesUseCase = GetAppLicensesUseCase.Impl(licensesRepository)

    @Provides
    fun providesGetLicenseUseCase(licensesRepository: LicensesRepository)
            : GetLicenseUseCase = GetLicenseUseCase.Impl(licensesRepository)

    @Provides
    fun providesRefreshDataUseCase(languageRepository: LanguageRepository,
                                   supportRepository: SupportRepository)
            : RefreshDataUseCase = RefreshDataUseCase.Impl(languageRepository, supportRepository)

}