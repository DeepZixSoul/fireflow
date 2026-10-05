package com.fireflow.exportaciones

import android.content.Context
import androidx.compose.runtime.Immutable
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.ClientRepository
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@Immutable
data class ExportUiState(
    val isExporting: Boolean = false,
    val exportedFile: String? = null,
    val error: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository,
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val revisionId: Long = savedStateHandle.get<String>("revisionId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun export() {
        viewModelScope.launch {
            _uiState.value = ExportUiState(isExporting = true)

            try {
                val revision = revisionRepository.getRevisionById(revisionId).getOrNull()
                    ?: throw Exception("Revisión no encontrada")

                val group = groupRepository.getGroupById(revision.groupId).getOrNull()
                    ?: throw Exception("Grupo no encontrado")

                val client = clientRepository.getClientById(group.clientId).getOrNull()
                    ?: throw Exception("Cliente no encontrado")

                val motors = motorRepository.getMotorsByGroup(revision.groupId).first()
                val year = Calendar.getInstance().apply { timeInMillis = revision.date }.get(Calendar.YEAR)
                val measurementsList = if (motors.isNotEmpty()) {
                    measurementRepository.getByMotorsAndYear(motors.map { it.id }, year)
                } else {
                    emptyList()
                }
                val measurements = motors.associate { motor ->
                    motor.id to measurementsList.find { it.motorId == motor.id }
                }

                val csv = CsvBuilder.build(client, group, revision, motors, measurements)

                val dir = File(context.cacheDir, "exports")
                dir.mkdirs()
                val file = File(dir, "revision_${revision.id}.csv")
                file.writeText(csv, Charsets.UTF_8)

                _uiState.value = ExportUiState(isExporting = false, exportedFile = file.absolutePath)
            } catch (e: Exception) {
                _uiState.value = ExportUiState(isExporting = false, error = e.message)
            }
        }
    }

    fun shareFile(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartir").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
