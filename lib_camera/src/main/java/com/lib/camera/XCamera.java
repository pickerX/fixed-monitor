package com.lib.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.lib.camera.view.AutoFitSurfaceView;
import com.lib.record.Config;
import com.lib.record.Monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Camera2 实现
 *
 * @author pickerx
 * @date 2022/1/28 12:29 下午
 */
public class XCamera {
    static final String TAG = "XCamera";

    private final Config config;
    private CameraManager mCameraManager;
    private CameraCharacteristics mCharacteristics;

    private Surface mRecordSurface;
    private final AutoFitSurfaceView mPreviewSurface;
    private List<Surface> mTargets;

    private CameraCaptureSession mSession;
    private CameraDevice mCameraDevice;

    private CaptureRequest mPreviewRequest;
    private CaptureRequest mRecordRequest;
    private MediaRecorder mRecorder;
    /**
     * [HandlerThread] where all camera operations run
     */
    private final HandlerThread cameraThread = new HandlerThread("CameraThread");

    /**
     * [Handler] corresponding to [cameraThread]
     */
    private final Handler cameraHandler;

    private static final int RECORDER_VIDEO_BITRATE = 10_000_000;
    private Context mContext;
    private CameraInfo mFront;

    private File mOutputFile;
    /**
     * lifecycle of camera callback
     */
    private CameraLifecycle cameraLifecycle;

    public XCamera(Config config) {
        this.config = config;
        cameraThread.start();
        mPreviewSurface = config.target;
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private int rotation = Surface.ROTATION_0;

    private void initListener() {
        // Sets output orientation based on current sensor value at start time
        OrientationEventListener listener = new OrientationEventListener(mContext) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation <= 45) rotation = Surface.ROTATION_0;
                if (orientation <= 135) rotation = Surface.ROTATION_90;
                if (orientation <= 225) rotation = Surface.ROTATION_180;
                if (orientation <= 315) rotation = Surface.ROTATION_270;
                else rotation = Surface.ROTATION_0;
            }
        };
        listener.enable();
    }

    public void prepare(Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initListener();
        try {
            Log.d(TAG, "Flow: 1. fetch support cameras");
            List<CameraInfo> cameras = enumerateVideoCameras(mCameraManager);
            // get best camera for now
            mFront = cameras.get(0);
            Log.d(TAG, "Flow: 2. find best camera:" + mFront);
            mCharacteristics = mCameraManager.getCameraCharacteristics(mFront.cameraId);
            mOutputFile = CameraUtils.createFile(mContext, config.directory, "mp4");
            Log.d(TAG, "File prepared >>>>>" + mOutputFile.getAbsolutePath());

            if (config.preview) {
                mRecordSurface = createRecordSurface(mFront);
                Log.d(TAG, "record surface prepared!!");
            }
            if (mPreviewSurface == null && config.preview) {
                Log.e(TAG, "output target cannot be null");
                return;
            }
            // only support preview for now
            if (mPreviewSurface == null) return;
            Log.d(TAG, "Flow: 3. prepare surfaceView");
            SurfaceHolder holder = mPreviewSurface.getHolder();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    // Selects appropriate preview size and configures view finder
                    Size previewSize = CameraUtils.getPreviewOutputSize(
                            mPreviewSurface.getDisplay(), mCharacteristics, SurfaceHolder.class, 0);
                    if (previewSize == null) {
                        Log.e(TAG, "Preview size cannot be null");
                        return;
                    }
                    Log.d(TAG, "Flow: 4. find best preview size:" + previewSize);
                    try {
                        mPreviewSurface.setAspectRatio(previewSize.getHeight(),
                                previewSize.getWidth());
                        // To ensure that size is set, initialize camera in the view's thread
                        if (cameraLifecycle != null) cameraLifecycle.onPrepared();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

                }
            });
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "MediaRecorder prepare failed");
        }

    }

    public void start() {
        if (mPreviewSurface == null && config.preview) {
            Log.e(TAG, "output target cannot be null");
            return;
        }
        assert mPreviewSurface != null;
        mPreviewSurface.post(() -> {
            Log.d(TAG, "Flow: 5. prepare done!");
            initializeCamera(mFront.cameraId);
        });
    }

    /**
     * release all camera resource
     * release context
     */
    public void release() {
        if (mSession != null) mSession.close();
        if (mCameraDevice != null) mCameraDevice.close();
        mContext = null;
    }

    private void initializeCamera(String cameraId) {
        try {
            Log.d(TAG, "Flow: 6. open camera");
            openCamera(mCameraManager, cameraId, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera(CameraManager cameraManager,
                            String cameraId,
                            Handler handler) throws CameraAccessException {
        CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                Log.d(TAG, "Flow: 7. Camera " + cameraId + " has been opened!!");
                try {
                    Log.d(TAG, "Flow: 8. creating camera capture session");
                    mCameraDevice = cameraDevice;
                    mTargets = getTargets();
                    createCaptureSession(cameraDevice, mTargets, handler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                Log.w(TAG, "Camera " + cameraId + " has been disconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                Log.e(TAG, "Camera " + cameraId + " open failed!!");
                switch (i) {
                    case ERROR_CAMERA_DISABLED:
                        Log.e(TAG, ">>> error camera disabled");
                        break;
                    case ERROR_CAMERA_DEVICE:
                        Log.e(TAG, ">>> error camera device");
                        break;
                    case ERROR_CAMERA_IN_USE:
                        Log.e(TAG, ">>> error camera in use");
                        break;
                    case ERROR_CAMERA_SERVICE:
                        Log.e(TAG, ">>> error camera service");
                        break;
                    case ERROR_MAX_CAMERAS_IN_USE:
                        Log.e(TAG, ">>> error max cameras in use");
                        break;
                    default:
                        Log.e(TAG, ">>> unknown");
                        break;
                }
            }
        };

        cameraManager.openCamera(cameraId, callback, handler);
    }

    private void createCaptureSession(
            CameraDevice device,
            List<Surface> targets,
            Handler handler) throws CameraAccessException {
        CameraCaptureSession.StateCallback callback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                Log.d(TAG, "Flow: 9. Session configured!!");
                try {
                    mSession = cameraCaptureSession;
                    Log.d(TAG, "Flow: 10. create preview and record requests!!");
                    mPreviewRequest = createPreviewRequest(targets.get(0), mSession);
                    mRecordRequest = createVideoRequest(mSession,
                            mPreviewSurface.getHolder().getSurface(),
                            mRecordSurface, mFront.fps);
                    mSession.setRepeatingRequest(mPreviewRequest, null, handler);
                    start(mSession, handler);
                } catch (CameraAccessException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                Log.e(TAG, "Camera " + device.getId() + " session configuration failed");
                mSession = null;
            }
        };

        device.createCaptureSession(targets, callback, handler);
    }

    private long recordingStartMillis = 0;
    private long recordingStopMillis = 0;

    private void start(CameraCaptureSession session, Handler handler) throws CameraAccessException, IOException {
        if (config.preview) {
            // Prevents screen rotation during the video recording
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
        // Start recording repeating requests, which will stop the ongoing preview
        // repeating requests without having to explicitly call `session.stopRepeating`
        session.setRepeatingRequest(mRecordRequest, null, handler);
        // Finalizes recorder setup and starts recording
        mRecorder = createRecorder(mRecordSurface,
                mFront.fps,
                mFront.size.getWidth(),
                mFront.size.getHeight(),
                mOutputFile.getAbsolutePath());
        // rotate by orientation
        int orientation = CameraUtils.computeRelativeRotation(mCharacteristics, rotation);
        Log.d(TAG, "record orientation:" + orientation);
        mRecorder.setOrientationHint(orientation);
        mRecorder.prepare();
        mRecorder.start();

        recordingStartMillis = System.currentTimeMillis();
        Log.d(TAG, "Flow: 11. Finalizes Recording started at " + recordingStartMillis);
        Log.d(TAG, "Recording started");

        if (cameraLifecycle != null) cameraLifecycle.onStarted(recordingStartMillis);
        // Starts recording animation
        // fragmentCameraBinding.overlay.post(animationTask);
    }

    public void stop() {
        if (mContext == null) return;

        if (config.preview) {
            // Prevents screen rotation during the video recording
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

        Log.d(TAG, "Recording stopped. Output file:" + mOutputFile.getAbsolutePath());
        mRecorder.stop();
        recordingStopMillis = System.currentTimeMillis();

        // Removes recording animation
        // fragmentCameraBinding.overlay.removeCallbacks(animationTask);

        // Broadcasts the media file to the rest of the system
        MediaScannerConnection.scanFile(
                mContext, new String[]{mOutputFile.getAbsolutePath()}, null, null);

        //保存第一帧图片
        String cover = mOutputFile.getAbsolutePath().replace(mOutputFile.getName(), "") + mOutputFile.getName() + ".jpg";
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mOutputFile.getAbsolutePath());
            Bitmap bitmap = retriever.getFrameAtTime();
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(new File(cover));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            retriever.release();
        } catch (Exception e) {
        }
        // request next recording
        if (cameraLifecycle != null)
            cameraLifecycle.onStopped(recordingStopMillis, mOutputFile.getName(), mOutputFile.getAbsolutePath(), mOutputFile.length(), recordingStopMillis - recordingStartMillis, cover);

    }

    public void restart() {
        mOutputFile = CameraUtils.createFile(mContext, config.directory, "mp4");
        mPreviewSurface.post(() -> {
            Log.d(TAG, "Flow: new capture session, start next record!");
            try {
                createCaptureSession(mCameraDevice, mTargets, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private Surface createRecordSurface(CameraInfo front) throws IOException {
        Surface surface = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            surface = MediaCodec.createPersistentInputSurface();
            MediaRecorder mr = createRecorder(surface,
                    front.fps,
                    front.size.getWidth(),
                    front.size.getHeight(),
                    mOutputFile.getAbsolutePath());
            mr.prepare();
            mr.release();
        }

        return surface;
    }

    private MediaRecorder createRecorder(Surface surface,
                                         int fps,
                                         int width,
                                         int height,
                                         String directory) {
        // check path valid
        MediaRecorder mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mr.setOutputFile(directory);
        mr.setVideoEncodingBitRate(RECORDER_VIDEO_BITRATE);
        if (fps > 0) mr.setVideoFrameRate(fps);

        mr.setVideoSize(width, height);
        mr.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mr.setInputSurface(surface);
        }
        return mr;
    }

    /**
     * 构造预览 request
     */
    private CaptureRequest createPreviewRequest(Surface outputTarget, CameraCaptureSession session) throws CameraAccessException {
        if (session == null) {
            Log.e(TAG, "No Session created!!");
            return null;
        }
        CaptureRequest.Builder b = session.getDevice()
                .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        b.addTarget(outputTarget);
        return b.build();
    }

    private CaptureRequest createVideoRequest(CameraCaptureSession session,
                                              Surface outputTargetView,
                                              Surface outputTarget,
                                              int fps) throws CameraAccessException {
        CaptureRequest.Builder request = session.getDevice()
                .createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        request.addTarget(outputTargetView);
        request.addTarget(outputTarget);
        request.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(fps, fps));

        return request.build();
    }

    /**
     * Converts a lens orientation enum into a human-readable string
     */
    private String lensOrientationString(int value) {
        String type;
        switch (value) {
            case CameraCharacteristics.LENS_FACING_BACK:
                type = Monitor.FACING_BACK;
                break;
            case CameraCharacteristics.LENS_FACING_FRONT:
                type = Monitor.FACING_FRONT;
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                type = Monitor.FACING_EXTERNAL;
                break;
            default:
                type = Monitor.FACING_UNKNOWN;
                break;
        }
        return type;
    }

    private List<Surface> getTargets() {
        return Arrays.asList(mPreviewSurface.getHolder().getSurface(), mRecordSurface);
    }

    /**
     * Lists all video-capable cameras and supported resolution and FPS combinations
     */
    private List<CameraInfo> enumerateVideoCameras(CameraManager cameraManager) throws CameraAccessException {
        List<CameraInfo> availableCameras = new ArrayList<>();

        // Iterate over the list of cameras and add those with high speed video recording
        // capability to our output. This function only returns those cameras that declare
        // constrained high speed video recording, but some cameras may be capable of doing
        // unconstrained video recording with high enough FPS for some use cases and they will
        // not necessarily declare constrained high speed video capability.
        String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
            String orientation = lensOrientationString(
                    characteristics.get(CameraCharacteristics.LENS_FACING));
            // Query the available capabilities and output formats
            int[] capabilities = characteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            StreamConfigurationMap cameraConfig = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            boolean filter = config.cameraOrientation.equals(orientation);

            // Return cameras that declare to be backward compatible
            if (filter && hasCapability(capabilities, CameraCharacteristics
                    .REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)) {
                // Recording should always be done in the most efficient format, which is
                //  the format native to the camera framework
                Class<MediaRecorder> targetClass = MediaRecorder.class;
                // For each size, list the expected FPS
                Size[] sizes = cameraConfig.getOutputSizes(targetClass);
                for (Size size : sizes) {
                    // Get the number of seconds that each frame will take to process
                    double secondsPerFrame = cameraConfig.getOutputMinFrameDuration(targetClass, size) /
                            1_000_000_000.0;
                    // Compute the frames per second to let user select a configuration
                    int fps = 0;
                    if (secondsPerFrame > 0)
                        fps = (int) (1.0 / secondsPerFrame);

                    String fpsLabel;
                    if (fps > 0)
                        fpsLabel = "$fps";
                    else
                        fpsLabel = "N/A";

                    String name = String.format(Locale.getDefault(),
                            "%s (%s) %s %s FPS", orientation, id, size.toString(), fps);
                    CameraInfo c = new CameraInfo(name, id, size, fps);
                    availableCameras.add(c);
                    Log.d(TAG, c.name);
                }
            }
        }

        return availableCameras;
    }

    private boolean hasCapability(int[] capabilities, int request) {
        for (int capability : capabilities) {
            if (request == capability) {
                return true;
            }
        }
        return false;
    }

    public void bindLifecycle(CameraLifecycle cameraLifecycle) {
        this.cameraLifecycle = cameraLifecycle;
    }
}