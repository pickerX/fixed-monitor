package com.lib.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lib.camera.extension.getAspectRatio
import com.lib.record.Config

/**
 *
 * @author pengfei.huang
 * create on 2022/3/9
 */
class CameraX(val config: Config) {

    private val captureLiveStatus = MutableLiveData<String>()
    private val cameraCapabilities = mutableListOf<CameraCapability>()

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private lateinit var recordingState: VideoRecordEvent
    private lateinit var viewLifecycleOwner: LifecycleOwner

    private var cameraIndex = 0
    private var qualityIndex = DEFAULT_QUALITY_IDX
    private var audioEnabled = false

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(context) }

    // private var enumerationDeferred: Deferred<Unit>? = null
    var cameraLifecycle: CameraLifecycle? = null
    private lateinit var context: Context

    /**
     * Query and cache this platform's camera capabilities, run only once.
     */
    fun prepare(context: Context) {
        this.context = context
        viewLifecycleOwner =
            if (context is FragmentActivity) context
            else context as Fragment

        val provider = ProcessCameraProvider.getInstance(context).get()
        provider.unbindAll()

        for (camSelector in arrayOf(
            CameraSelector.DEFAULT_BACK_CAMERA,
//            CameraSelector.DEFAULT_FRONT_CAMERA
        )) {
            try {
                // just get the camera.cameraInfo to query capabilities
                // we are not binding anything here.
                if (provider.hasCamera(camSelector)) {
                    val camera = provider.bindToLifecycle(viewLifecycleOwner, camSelector)
                    QualitySelector.getSupportedQualities(camera.cameraInfo)
                        .filter { quality ->
                            listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                                .contains(quality)
                        }.also {
                            cameraCapabilities.add(CameraCapability(camSelector, it))
                        }
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Camera Face $camSelector is not supported")
            }
        }
        Log.e(TAG, "find camera, bindCaptureUseCase")

        bindCaptureUsecase()
    }

    fun startRecord() {
        startRecording()
    }

    /**
     *   Always bind preview + video capture use case combinations in this sample
     *   (VideoCapture can work on its own). The function should always execute on
     *   the main thread.
     */
    private fun bindCaptureUsecase() {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val cameraSelector = getCameraSelector(cameraIndex)

        // create the user required QualitySelector (video resolution): we know this is
        // supported, a valid qualitySelector will be created.
        val quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)

        val preview = Preview.Builder()
            .setTargetAspectRatio(quality.getAspectRatio(quality))
            .build().apply {
                setSurfaceProvider(config.previewTarget.surfaceProvider)
            }

        // build a recorder, which can:
        //   - record video/audio to MediaStore(only shown here), File, ParcelFileDescriptor
        //   - be used create recording(s) (the recording performs recording)
        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                videoCapture,
                preview
            )
            Log.e(TAG, "Camera prepared.")
            cameraLifecycle?.onPrepared()
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /**
     * Kick start the video recording
     *   - config Recorder to capture to MediaStoreOutput
     *   - register RecordEvent Listener
     *   - apply audio request from user
     *   - start recording!
     * After this function, user could start/pause/resume/stop recording and application listens
     * to VideoRecordEvent for the current recording status.
     */
    @SuppressLint("MissingPermission")
    private fun startRecording() {
        Log.i(TAG, "startRecording")

        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val file = CameraUtils.createFile(context, config.directory, ".mp4")

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .apply {
                if (audioEnabled) withAudioEnabled()
            }
            .start(mainThreadExecutor) { event ->
                onEventCallback(event)
            }

        Log.i(TAG, "Recording started")
    }

    fun stopRecord() {
        currentRecording?.stop()
        currentRecording = null
    }

    /**
     * CaptureEvent listener.
     */
    private fun onEventCallback(event: VideoRecordEvent) {
        // cache the recording state
        if (event !is VideoRecordEvent.Status) {
            Log.e(TAG, "receive event:$event")
            recordingState = event
        }
        when (event) {
            is VideoRecordEvent.Status -> {
                // placeholder: we update the UI with new status after this when() block,
                // nothing needs to do here.
            }
            is VideoRecordEvent.Start -> {
                // Handle the start of a new active recording
                cameraLifecycle?.onStarted(System.currentTimeMillis())
            }
            is VideoRecordEvent.Pause -> {

            }
            // Handle the case where the active recording is paused
            is VideoRecordEvent.Resume -> {}
            // Handles the case where the active recording is resumed
            is VideoRecordEvent.Finalize -> {
                // Handles a finalize event for the active recording, checking Finalize.getError()
                // record finished
                cameraLifecycle?.onStopped(
                    System.currentTimeMillis(),
                    "name",
                    "path",
                    1,
                    0,
                    "corverPath"
                )
            }
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${event}: recorded ${size}KB, in ${time}second"
        if (event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        captureLiveStatus.value = text
        Log.i(TAG, "recording event: $text")
    }

    /**
     * Retrieve the asked camera's type(lens facing type). In this sample, only 2 types:
     *   idx is even number:  CameraSelector.LENS_FACING_BACK
     *          odd number:   CameraSelector.LENS_FACING_FRONT
     */
    private fun getCameraSelector(idx: Int): CameraSelector {
        if (cameraCapabilities.size == 0) {
            Log.e(TAG, "Error: This device does not have any camera, bailing out")
            // context.finish()
        }
        return cameraCapabilities[idx % cameraCapabilities.size].camSelector
    }

    fun release() {
        stopRecord()
        currentRecording?.close()
    }

    data class CameraCapability(val camSelector: CameraSelector, val qualities: List<Quality>)

    companion object {
        // default Quality selection if no input from UI
        const val DEFAULT_QUALITY_IDX = 0
        const val TAG = "CameraX"
    }
}