package com.igrupos.informes

import android.content.Context
import androidx.compose.runtime.Immutable
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.ClientRepository
import com.igrupos.domain.repository.MotorRepository
import com.igrupos.domain.repository.PhotoRepository
import com.igrupos.domain.repository.PressureGroupRepository
import com.igrupos.domain.repository.PressureMeasurementRepository
import com.igrupos.domain.repository.RevisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Immutable
data class ReportUiState(
    val isGenerating: Boolean = false,
    val pdfFile: String? = null,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository,
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository,
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val pdfGenerator = PdfGenerator(context)

    private val revisionId: Long = savedStateHandle.get<String>("revisionId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun generatePdf() {
        viewModelScope.launch {
            _uiState.value = ReportUiState(isGenerating = true)

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

                val photos = photoRepository.getPhotosByRevision(revisionId).getOrNull() ?: emptyList()

                val file = pdfGenerator.generateReport(client, group, revision, motors, measurements, photos)

                _uiState.value = ReportUiState(isGenerating = false, pdfFile = file.absolutePath)
            } catch (e: Exception) {
                _uiState.value = ReportUiState(isGenerating = false, error = e.message)
            }
        }
    }

    fun sharePdf(filePath: String) {
        val file = java.io.File(filePath)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartir PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
