package com.jpp.mpperson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.jpp.mp.common.coroutines.CoroutineDispatchers
import com.jpp.mp.common.coroutines.MPScopedViewModel
import com.jpp.mp.common.navigation.Destination
import com.jpp.mpdomain.Person
import com.jpp.mpperson.PersonInteractor.PersonEvent.AppLanguageChanged
import com.jpp.mpperson.PersonInteractor.PersonEvent.NotConnectedToNetwork
import com.jpp.mpperson.PersonInteractor.PersonEvent.Success
import com.jpp.mpperson.PersonInteractor.PersonEvent.UnknownError
import com.jpp.mpperson.PersonRowViewState.Companion.bioRow
import com.jpp.mpperson.PersonRowViewState.Companion.birthdayRow
import com.jpp.mpperson.PersonRowViewState.Companion.deathDayRow
import com.jpp.mpperson.PersonRowViewState.Companion.emptyRow
import com.jpp.mpperson.PersonRowViewState.Companion.placeOfBirthRow
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [MPScopedViewModel] that supports the person section. The VM retrieves
 * the data from the underlying layers using the provided [PersonInteractor] and maps the business
 * data to UI data, producing a [PersonViewState] that represents the configuration of the view.
 *
 * This VM is language aware, meaning that when the user changes the language of the device, the
 * VM is notified about such event and executes a refresh of both: the data stored by the application
 * and the view state being shown to the user.
 */
class PersonViewModel @Inject constructor(
    dispatchers: CoroutineDispatchers,
    private val personInteractor: PersonInteractor
) :
        MPScopedViewModel(dispatchers) {

    private val _viewState = MediatorLiveData<PersonViewState>()
    val viewState: LiveData<PersonViewState> get() = _viewState

    private lateinit var currentParam: PersonParam

    private val retry: () -> Unit = { fetchPersonData(currentParam.personName, currentParam.imageUrl, currentParam.personId) }

    /*
     * Map the business logic coming from the interactor into view layer logic.
     */
    init {
        _viewState.addSource(personInteractor.events) { event ->
            when (event) {
                is NotConnectedToNetwork -> _viewState.value = PersonViewState.showNoConnectivityError(currentParam.personName, retry)
                is UnknownError -> _viewState.value = PersonViewState.showUnknownError(currentParam.personName, retry)
                is Success -> _viewState.value = getViewStateFromPersonData(currentParam.personName, currentParam.imageUrl, event.person)
                is AppLanguageChanged -> refreshPersonData(currentParam.personName, currentParam.imageUrl, currentParam.personId)
            }
        }
    }

    /**
     * Called on VM initialization. The View (Fragment) should call this method to
     * indicate that it is ready to start rendering. When the method is called, the VM
     * internally verifies the state of the application and updates the view state based
     * on it.
     */
    fun onInit(param: PersonParam) {
        updateCurrentDestination(Destination.ReachedDestination(param.personName))
        currentParam = param
        fetchPersonData(currentParam.personName, currentParam.imageUrl, currentParam.personId)
    }

    /**
     * When called, this method will push the loading view state and will fetch the data
     * of the person identified by [personId]. When the fetching process is done, the view state will be updated
     * based on the result posted by the interactor.
     */
    private fun fetchPersonData(personName: String, personImageUrl: String, personId: Double) {
        withInteractor { fetchPerson(personId) }
        _viewState.value = PersonViewState.showLoading(personName, personImageUrl)
    }

    /**
     * Starts a refresh data process by indicating to the interactor that new data needs
     * to be fetched for the person being shown. This is executed in a background
     * task while the view state is updated with the loading state.
     */
    private fun refreshPersonData(personName: String, personImageUrl: String, personId: Double) {
        withInteractor {
            flushPersonData()
            fetchPerson(personId)
        }
        _viewState.value = PersonViewState.showLoading(personName, personImageUrl)
    }

    /**
     * Helper function to execute an [action] in the [personInteractor] instance
     * on a background task.
     */
    private fun withInteractor(action: PersonInteractor.() -> Unit) {
        launch { withContext(dispatchers.default()) { action(personInteractor) } }
    }

    /**
     * Creates a [PersonViewState] that represents the data to show from the provided
     * [person].
     */
    private fun getViewStateFromPersonData(personName: String, personImageUrl: String, person: Person): PersonViewState {
        return when (person.isEmpty()) {
            true -> PersonViewState.showNoDataAvailable(personName, personImageUrl)
            else -> PersonViewState.showPerson(personName, personImageUrl, mapPersonData(person))
        }
    }

    private fun mapPersonData(person: Person): PersonContentViewState {
        return PersonContentViewState(
                birthday = person.birthday?.let { birthdayRow(it) } ?: emptyRow(),
                placeOfBirth = person.place_of_birth?.let { placeOfBirthRow(it) } ?: emptyRow(),
                deathDay = person.deathday?.let { deathDayRow(it) } ?: emptyRow(),
                bio = if (person.biography.isEmpty()) emptyRow() else bioRow(person.biography)
        )
    }

    private fun Person.isEmpty() = biography.isEmpty() &&
            birthday.isNullOrEmpty() &&
            deathday.isNullOrEmpty() &&
            place_of_birth.isNullOrEmpty()
}
