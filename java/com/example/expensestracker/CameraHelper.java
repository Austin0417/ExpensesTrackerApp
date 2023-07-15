package com.example.expensestracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;


public class CameraHelper {
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
    public static String getRearCameraId(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            String cameraIds[] = cameraManager.getCameraIdList();
            for (int i = 0; i < cameraIds.length; i++) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[i]);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) != null &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = cameraIds[i];
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
    }

}
