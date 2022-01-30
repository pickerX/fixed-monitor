package com.lib.record;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author pickerx
 * @date 2022/1/28 12:29 下午
 */
public class XCamera {
    static final String TAG = "XCamera";

    private final Config config;
    private CameraManager mCameraManager;
    private CameraCharacteristics mCharacteristics;
    /**
     * 预览的 View，按需初始化
     */
    private Surface mPreviewSurface;
    private CameraCaptureSession mSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest mRecordRequest;

    /**
     * [HandlerThread] where all camera operations run
     */
    private final HandlerThread cameraThread = new HandlerThread("CameraThread");

    /**
     * [Handler] corresponding to [cameraThread]
     */
    private final Handler cameraHandler;

    private static final int RECORDER_VIDEO_BITRATE = 10_000_000;

    public XCamera(Config config) {
        this.config = config;
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    public void init(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        ImageCapture imageCapture =
                new ImageCapture.Builder()
                        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .build();

//        ViewPort viewPort = new ViewPort.Builder(
//                new Rational(width, height),
//                getDisplay().getRotation()).build();
//        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
//                .addUseCase(preview)
//                .addUseCase(imageAnalysis)
//                .addUseCase(imageCapture)
//                .setViewPort(viewPort)
//                .build();
//        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup);
        try {
            List<CameraInfo> cameras = enumerateVideoCameras(mCameraManager);
            CameraInfo front = cameras.get(0);
            mCharacteristics = mCameraManager.getCameraCharacteristics(front.cameraId);
            if (config.preview) {
                mPreviewSurface = createSurface(front);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "MediaRecorder prepare failed");
        }
    }

    private Surface createSurface(CameraInfo front) throws IOException {
        Surface surface = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            surface = MediaCodec.createPersistentInputSurface();
            createRecorder(surface, front.fps, front.size.getWidth(), front.size.getHeight());
        }

        return surface;
    }

    private void createRecorder(Surface surface, int fps, int width, int height) throws IOException {
        MediaRecorder mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // TODO change path
        mr.setOutputFile(config.directory);
        mr.setVideoEncodingBitRate(RECORDER_VIDEO_BITRATE);
        if (fps > 0) mr.setVideoFrameRate(fps);

        mr.setVideoSize(width, height);
        mr.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mr.setInputSurface(surface);
        }
        mr.prepare();
        mr.release();
    }

    /**
     * 构造预览 request
     */
    private CaptureRequest createPreviewRequest(Surface outputTarget) throws CameraAccessException {
        CaptureRequest.Builder b = mSession.getDevice()
                .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        b.addTarget(outputTarget);
        return b.build();
    }

    private void setupListeners(Context context, ImageCapture imageCapture) {
        // 屏幕旋转时，旋转摄像头录制角度
        OrientationEventListener orientationEventListener =
                new OrientationEventListener(context) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        int rotation;

                        // Monitors orientation values to determine the target rotation value
                        if (orientation >= 45 && orientation < 135) {
                            rotation = Surface.ROTATION_270;
                        } else if (orientation >= 135 && orientation < 225) {
                            rotation = Surface.ROTATION_180;
                        } else if (orientation >= 225 && orientation < 315) {
                            rotation = Surface.ROTATION_90;
                        } else {
                            rotation = Surface.ROTATION_0;
                        }

                        imageCapture.setTargetRotation(rotation);
                    }
                };

        orientationEventListener.enable();
    }

    /**
     * Converts a lens orientation enum into a human-readable string
     */
    private String lensOrientationString(int value) {
        String type;
        switch (value) {
            case CameraCharacteristics.LENS_FACING_BACK:
                type = "Back";
                break;
            case CameraCharacteristics.LENS_FACING_FRONT:
                type = "Front";
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                type = "External";
                break;
            default:
                type = "Unknown";
                break;
        }
        return type;
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

            // Return cameras that declare to be backward compatible
            if (hasCapability(capabilities, CameraCharacteristics
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
                            "%s (%s) %s %s FPS", orientation, id, size.toString(), fpsLabel);
                    availableCameras.add(
                            new CameraInfo(name, id, size, fps));
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
}

class CameraInfo {
    String name;
    String cameraId;
    Size size;
    int fps;

    public CameraInfo(String name, String cameraId, Size size, int fps) {
        this.name = name;
        this.cameraId = cameraId;
        this.size = size;
        this.fps = fps;
    }
}