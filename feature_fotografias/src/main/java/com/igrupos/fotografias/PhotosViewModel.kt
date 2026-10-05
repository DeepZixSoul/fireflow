package com.igrupos.fotografias

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.PhotoRepository
import com.igrupos.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val revisionId: Long = savedStateHandle.get<String>("revisionId")?.toLongOrNull() ?: -1L

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    val photos: StateFlow<List<Photo>> = photoRepository.getPhotosByRevisionFlow(revisionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPhotoFile(): File {
        val dir = File(context.filesDir, "photos/$revisionId")
        dir.mkdirs()
        val filename = "photo_${System.currentTimeMillis()}.jpg"
        return File(dir, filename)
    }

    fun savePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _isCapturing.value = true

            val result = withContext(Dispatchers.IO) {
                try {
                    val file = getPhotoFile()
                    val compressed = compressImage(bitmap)
                    val stripped = stripExifMetadata(compressed)

                    FileOutputStream(file).use { out ->
                        stripped.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }

                    val photo = Photo(
                        id = 0L,
                        revisionId = revisionId,
                        filePath = file.absolutePath,
                        thumbnailPath = file.absolutePath,
                        timestamp = System.currentTimeMillis(),
                        sizeBytes = file.length()
                    )

                    photoRepository.savePhoto(photo)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            _isCapturing.value = false
        }
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        val maxDimension = 1920
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun stripExifMetadata(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            val photo = photos.value.find { it.id == photoId }
            photo?.let {
                File(it.filePath).delete()
                photoRepository.deletePhoto(photoId)
            }
        }
    }
}
