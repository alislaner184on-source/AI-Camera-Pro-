package com.aicamerapro

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
 private lateinit var previewView: PreviewView
 private lateinit var status: TextView
 private lateinit var shutter: Button
 private lateinit var mode: Button
 private lateinit var flip: Button
 private var camera: Camera? = null
 private var imageCapture: ImageCapture? = null
 private var videoCapture: VideoCapture<Recorder>? = null
 private var recording: Recording? = null
 private var front = false
 private var videoMode = false
 private var zoom = 1f
 private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
  if (granted[Manifest.permission.CAMERA] == true) startCamera() else showStatus("Permissão de câmera necessária")
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xff10151c.toInt()) }
  status = TextView(this).apply { text = "AI Camera Pro • 0.1"; setTextColor(-1); gravity = Gravity.CENTER; textSize = 16f; setPadding(8,16,8,16) }
  previewView = PreviewView(this).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
  val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
  mode = Button(this).apply { text = "Foto / Vídeo"; setOnClickListener { if (recording == null) { videoMode = !videoMode; updateMode() } } }
  shutter = Button(this).apply { text = "Fotografar"; setOnClickListener { if (videoMode) toggleVideo() else takePhoto() } }
  flip = Button(this).apply { text = "Girar"; setOnClickListener { if (recording == null) { front = !front; startCamera() } } }
  val zoomOut = Button(this).apply { text = "−"; setOnClickListener { changeZoom(-0.5f) } }
  val zoomIn = Button(this).apply { text = "+"; setOnClickListener { changeZoom(0.5f) } }
  controls.addView(mode); controls.addView(shutter); controls.addView(flip)
  val zoomControls = LinearLayout(this).apply { gravity = Gravity.CENTER; addView(zoomOut); addView(zoomIn) }
  layout.addView(status)
  layout.addView(previewView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))
  layout.addView(zoomControls)
  layout.addView(controls)
  setContentView(layout)
  requestPermissionsIfNeeded()
 }
 private fun requestPermissionsIfNeeded() {
  val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
  if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
  else permissionLauncher.launch(permissions)
 }
 private fun showStatus(message: String) { runOnUiThread { status.text = message } }
 private fun updateMode() { shutter.text = if (videoMode) "Gravar" else "Fotografar"; showStatus(if (videoMode) "Modo vídeo" else "Modo foto") }
 private fun startCamera() {
  val future = ProcessCameraProvider.getInstance(this)
  future.addListener({
   try {
    val provider = future.get()
    provider.unbindAll()
    val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
    imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
    val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD, androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.HD))).build()
    videoCapture = VideoCapture.withOutput(recorder)
    camera = provider.bindToLifecycle(this, selector, preview, imageCapture, videoCapture)
    zoom = 1f
    showStatus("Câmera pronta • ${if (front) "frontal" else "traseira"}")
   } catch (e: Exception) { showStatus("Falha ao abrir câmera: ${e.message}") }
  }, ContextCompat.getMainExecutor(this))
 }
 private fun changeZoom(delta: Float) {
  val cam = camera ?: return
  val state = cam.cameraInfo.zoomState.value ?: return
  zoom = (zoom + delta).coerceIn(state.minZoomRatio, state.maxZoomRatio)
  cam.cameraControl.setZoomRatio(zoom)
  showStatus("Zoom %.1fx".format(Locale.US, zoom))
 }
 private fun timestamp() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
 private fun takePhoto() {
  val capture = imageCapture ?: return
  val values = ContentValues().apply {
   put(MediaStore.MediaColumns.DISPLAY_NAME, "AICamera_${timestamp()}.jpg")
   put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
   if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AI Camera Pro")
  }
  val options = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
  capture.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
   override fun onImageSaved(result: ImageCapture.OutputFileResults) { showStatus("Foto salva na galeria") }
   override fun onError(exception: ImageCaptureException) { showStatus("Erro na foto: ${exception.message}") }
  })
 }
 private fun toggleVideo() {
  val active = recording
  if (active != null) { active.stop(); return }
  val capture = videoCapture ?: return
  val values = ContentValues().apply {
   put(MediaStore.MediaColumns.DISPLAY_NAME, "AICamera_${timestamp()}.mp4")
   put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
   if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/AI Camera Pro")
  }
  val options = MediaStoreOutputOptions.Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(values).build()
  var pending: PendingRecording = capture.output.prepareRecording(this, options)
  if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) pending = pending.withAudioEnabled()
  recording = pending.start(ContextCompat.getMainExecutor(this)) { event ->
   when (event) {
    is VideoRecordEvent.Start -> { shutter.text = "Parar"; showStatus("Gravando vídeo") }
    is VideoRecordEvent.Finalize -> {
     recording = null; shutter.text = "Gravar"
     showStatus(if (event.hasError()) "Erro ao salvar vídeo: ${event.error}" else "Vídeo salvo na galeria")
    }
   }
  }
 }
 override fun onDestroy() { recording?.stop(); super.onDestroy() }
}
