package com.chatcamp.uikit.utils;

/**
 * Created by shubhamdhabhai on 05/03/18.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class FileUtils {

    public static final String APP_DIRECTORY = "Chat_data";
    public static final String PICTURE_DIRECTORY = "pictures";
    public static final String VIDEO_DIRECTORY = "video";
    public static final String AUDIO_DIRECTORY = "audio";
    public static final String VOICE_DIRECTORY = "voice";
    public static final String DOCUMENT_DIRECTORY = "document";
    public static final String MISSCELLANEOUS_DIRECTORY = "miscellaneous";
    public static final String SENT_DIRECTORY = "sent";

    /**
     * Method for return file path of Gallery image
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */

    public static String getPath(final Context context, final Uri uri) {
        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }

            //DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        Log.e("log", uri.toString());
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static File downloadFile(Context context, String downloadFilePath, String directory,
                                    DownloadFileListener downloadFileListener) {
        File file = null;
        try {
            File serverFile = new File(downloadFilePath);
            // remove the random number at the starting of the file name
            String fileName = serverFile.getName();
            fileName = fileName.replaceFirst(".*_", "");
            // create a new file, to save the downloaded file
            if (isExternalStorageWritable()) {

                file = new File(Environment.getExternalStoragePublicDirectory(directory),
                        serverFile.getName());
            } else {
                if (context == null) {
                    return null;
                }
                file = new File(context.getDir(directory, Context.MODE_PRIVATE), serverFile.getName());
            }
            if (file.exists()) {
                return file;
            }

            URL url = new URL(downloadFilePath);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
//
            // connect
            urlConnection.connect();

            // set the path where we want to save the file
            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are
            // downloading
            int totalsize = urlConnection.getContentLength();
            int downloadedSize = 0;

            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                final float per = ((float) downloadedSize / totalsize) * 100;
                if (downloadFileListener != null) {
                    downloadFileListener.downloadProgress((int) per);
                }
            }

            if (downloadFileListener != null) {
                downloadFileListener.downloadComplete();
            }

            // close the output stream when complete //
            fileOutput.close();

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File downloadFile(Context context, String downloadFilePath, Directory directory, boolean isSent,
                                    DownloadFileListener downloadFileListener) {
        File file = null;
        try {
            File serverFile = new File(downloadFilePath);
            // remove the random number at the starting of the file name
            String fileName = serverFile.getName();
            fileName = fileName.substring(fileName.indexOf('_') + 1);
            // create a new file, to save the downloaded file
            if (isExternalStorageWritable()) {
                file = getFile(directory, fileName, isSent);
            } else {
                if (context == null) {
                    return null;
                }
                file = new File(context.getDir(getDirectoryName(directory), Context.MODE_PRIVATE), serverFile.getName());
            }
            if (file != null && file.exists()) {
                return file;
            }

            URL url = new URL(downloadFilePath);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
//
            // connect
            urlConnection.connect();

            // set the path where we want to save the file
            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are
            // downloading
            int totalsize = urlConnection.getContentLength();
            int downloadedSize = 0;

            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                final float per = ((float) downloadedSize / totalsize) * 100;
                if (downloadFileListener != null) {
                    downloadFileListener.downloadProgress((int) per);
                }
            }

            if (downloadFileListener != null) {
                downloadFileListener.downloadComplete();
            }

            // close the output stream when complete //
            fileOutput.close();

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static boolean fileExists(@NonNull Context context, @NonNull String downloadFilePath,@NonNull String directory) {
        try {
            File file = null;
            File serverFile = new File(downloadFilePath);
            // create a new file, to save the downloaded file
            if (isExternalStorageWritable()) {
                file = new File(Environment.getExternalStoragePublicDirectory(directory),
                        serverFile.getName());
            } else {
                if (context == null) {
                    return false;
                }
                file = new File(context.getDir(directory, Context.MODE_PRIVATE), serverFile.getName());
            }
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean fileExists(@NonNull Context context,@NonNull String downloadFilePath, @NonNull Directory directory, boolean isSent) {
        try {
            File file = null;
            File serverFile = new File(downloadFilePath);
            String fileName = serverFile.getName();
            fileName = fileName.substring(fileName.indexOf('_') + 1);
            // create a new file, to save the downloaded file
            if (isExternalStorageWritable()) {
                file = getFile(directory, fileName, isSent);
                try {


                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(context, Uri.fromFile(file));

                    String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                    boolean isVideo = "yes".equals(hasVideo);
                    if (isVideo) {
                        return true;
                    } else {
                        file.delete();
                        return false;
                    }
                } catch (Exception e) {
                    file.delete();
                    return false;
                }
            } else {
                if (context == null) {
                    return false;
                }
                file = new File(context.getDir(getDirectoryName(directory), Context.MODE_PRIVATE), serverFile.getName());
            }
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static File getFile(Directory directory, String name, boolean isSent) throws IOException {
        File file = null;
        File appDir = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        if (Directory.PICTURES.equals(directory)) {
            file = makeDir(PICTURE_DIRECTORY, name, isSent);
        } else if (Directory.VIDEOS.equals(directory)) {
            file = makeDir(VIDEO_DIRECTORY, name, isSent);
        } else if (Directory.DOCUMENT.equals(directory)) {
            file = makeDir(DOCUMENT_DIRECTORY, name, isSent);
        } else if (Directory.MUSIC.equals(directory)) {
            file  = makeDir(AUDIO_DIRECTORY, name, isSent);
        } else if (Directory.VOICE.equals(directory)) {
            file  = makeDir(VOICE_DIRECTORY, name, isSent);
        } else {
            file = makeDir(MISSCELLANEOUS_DIRECTORY, name, isSent);
        }
        return file;
    }

    private static File makeDir(String dirName, String fileName, boolean isSent) {
        File directory = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY + "/" + dirName);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File sentDir = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY + "/" + dirName + "/" + SENT_DIRECTORY);
        if (!sentDir.exists()) {
            sentDir.mkdir();
        }
        return new File(Environment.getExternalStorageDirectory(),
                APP_DIRECTORY + "/" + dirName + "/" + (isSent ? SENT_DIRECTORY : "") + "/" + fileName);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static String getDirectoryName(Directory directory) {
        if(directory.equals(Directory.DOCUMENT)) {
            return Environment.DIRECTORY_DOCUMENTS;
        } else if(directory.equals(Directory.MUSIC)) {
            return Environment.DIRECTORY_MUSIC;
        } else if(directory.equals(Directory.PICTURES)) {
            return Environment.DIRECTORY_PICTURES;
        } else if(directory.equals(Directory.VIDEOS)) {
            return Environment.DIRECTORY_MOVIES;
        } else if(directory.equals(Directory.VOICE)) {
            return Environment.DIRECTORY_MUSIC;
        } else {
            return Environment.DIRECTORY_DOWNLOADS;
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
