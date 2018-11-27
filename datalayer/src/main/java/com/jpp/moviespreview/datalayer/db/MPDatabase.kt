package com.jpp.moviespreview.datalayer.db

import com.jpp.moviespreview.datalayer.AppConfiguration

/**
 * Defines the database that MoviesPreview uses to store data locally.
 */
interface MPDataBase {
    fun getStoredAppConfiguration() : AppConfiguration?
    fun updateAppConfiguration(appConfiguration: AppConfiguration)
}