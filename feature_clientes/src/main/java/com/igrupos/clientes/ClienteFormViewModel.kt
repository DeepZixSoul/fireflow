package com.igrupos.clientes

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.common.R
import com.igrupos.core.util.GeocoderUtil
import com.igrupos.domain.repository.ClientRepository
import com.igrupos.domain.model.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ClienteFormState(
    val name: String = "",
    val cif: String = "",
    val address: String = "",
    val province: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    @StringRes val nameError: Int? = null,
    @StringRes val cifError: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isGeocoding: Boolean = false,
    @StringRes val geocodeError: Int? = null,
    @StringRes val error: Int? = null
)

@HiltViewModel
class ClienteFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val clientId: Long? = savedStateHandle.get<String>("clientId")?.toLongOrNull()
    private var geocodeJob: Job? = null
    private var initialLoad = true

    private val _uiState = MutableStateFlow(ClienteFormState())
    val uiState: StateFlow<ClienteFormState> = _uiState.asStateFlow()

    init {
        if (clientId != null) {
            loadClient()
        }
    }

    private fun loadClient() {
        viewModelScope.launch {
            clientRepository.getClientById(clientId!!).fold(
                onSuccess = { client ->
                    _uiState.value = _uiState.value.copy(
                        name = client.name,
                        cif = client.cif,
                        address = client.address,
                        province = client.province,
                        contactPerson = client.contactPerson,
                        phone = client.phone,
                        email = client.email,
                        latitude = client.latitude,
                        longitude = client.longitude,
                        notes = client.notes
                    )
                    initialLoad = false
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = null) }
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onCifChange(value: String) {
        _uiState.value = _uiState.value.copy(cif = value, cifError = null)
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
        scheduleGeocode()
    }

    fun onProvinceChange(value: String) {
        _uiState.value = _uiState.value.copy(province = value)
        scheduleGeocode()
    }

    fun onContactPersonChange(value: String) {
        _uiState.value = _uiState.value.copy(contactPerson = value)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    private fun scheduleGeocode() {
        if (initialLoad) return
        geocodeJob?.cancel()
        geocodeJob = viewModelScope.launch {
            delay(500L)
            val state = _uiState.value
            if (state.address.isBlank() && state.province.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    latitude = null,
                    longitude = null,
                    isGeocoding = false,
                    geocodeError = null
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isGeocoding = true, geocodeError = null)
            val result = GeocoderUtil.geocode(context, state.address, state.province)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    latitude = result.first,
                    longitude = result.second,
                    isGeocoding = false,
                    geocodeError = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isGeocoding = false,
                    geocodeError = R.string.error_geocode
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = R.string.error_name_required)
            hasError = true
        }
        if (state.cif.isBlank()) {
            _uiState.value = _uiState.value.copy(cifError = R.string.error_cif_required)
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val client = Client(
                id = clientId ?: 0L,
                name = state.name,
                cif = state.cif.uppercase(),
                address = state.address,
                province = state.province,
                contactPerson = state.contactPerson,
                phone = state.phone,
                email = state.email,
                latitude = state.latitude,
                longitude = state.longitude,
                notes = state.notes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true
            )

            clientRepository.saveClient(client).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, error = null) }
            )
        }
    }
}
