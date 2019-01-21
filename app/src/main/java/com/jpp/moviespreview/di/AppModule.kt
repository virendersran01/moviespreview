package com.jpp.moviespreview.di

import android.content.Context
import com.jpp.moviespreview.MPApp
import com.jpp.moviespreview.screens.CoroutineDispatchers
import com.jpp.moviespreview.screens.CoroutineDispatchersImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides appModule scoped dependencies.
 */
@Module
class AppModule(private val appInstance: MPApp) {

    @Provides
    @Singleton
    fun providesContext(): Context = appInstance.applicationContext

    @Provides
    @Singleton
    fun providesCoroutineDispatchers(): CoroutineDispatchers = CoroutineDispatchersImpl()

}