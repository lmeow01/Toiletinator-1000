package hk.hku.cs.toiletinator1000

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionsUtils {
    companion object {
        /**
         * Function to check whether the given permissions are granted.
         */
        fun isPermissionGranted(
            permissions: Array<out String>,
            grantResults: IntArray,
            permission: String
        ): Boolean {
            for (i in permissions.indices) {
                if (permission == permissions[i]) {
                    return grantResults[i] == PackageManager.PERMISSION_GRANTED
                }
            }
            return false
        }

        /**
         * Function to request permissions. An alias for ActivityCompat.requestPermissions.
         */
        fun requestPermissions(activity: Activity, permissions: Array<out String>, requestCode: Int) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
            )
        }
    }
}