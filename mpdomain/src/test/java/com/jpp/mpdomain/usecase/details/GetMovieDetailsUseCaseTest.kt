package com.jpp.mpdomain.usecase.details

import com.jpp.mpdomain.SupportedLanguage
import com.jpp.mpdomain.repository.ConnectivityRepository
import com.jpp.mpdomain.repository.LanguageRepository
import com.jpp.mpdomain.repository.MoviePageRepository
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetMovieDetailsUseCaseTest {

    @RelaxedMockK
    private lateinit var moviePageRepository: MoviePageRepository
    @RelaxedMockK
    private lateinit var connectivityRepository: ConnectivityRepository
    @RelaxedMockK
    private lateinit var languageRepository: LanguageRepository

    private val language = SupportedLanguage.English

    private lateinit var subject: GetMovieDetailsUseCase

    @BeforeEach
    fun setUp() {
        subject = GetMovieDetailsUseCase.Impl(moviePageRepository, connectivityRepository, languageRepository)
        every { languageRepository.getCurrentAppLanguage() } returns language
    }


}