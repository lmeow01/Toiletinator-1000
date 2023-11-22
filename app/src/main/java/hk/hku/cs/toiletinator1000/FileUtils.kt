package hk.hku.cs.toiletinator1000

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage

class FileUtils {
    companion object {
        private val storage = Firebase.storage

        /**
         * Uploads a file to Firebase Storage and returns the UploadTask. The caller can then
         * attach listeners to the UploadTask to monitor the upload status.
         * @param uri The URI of the file to upload.
         * @param directory The directory to upload the file to. If null, the file will be uploaded
         * to the root directory.
         * @param fileName The name of the file to upload. If null, the file will be uploaded with
         * a timestamp as the file name.
         * @return The UploadTask for the upload.
         */
        fun uploadFile(uri: Uri, directory: String? = null, fileName: String? = null): UploadTask {
            val uploadRef =
                storage.reference.child("${directory ?: ""}/${fileName ?: System.currentTimeMillis()}")
            return uploadRef.putFile(uri)
        }
    }
}