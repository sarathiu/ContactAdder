package com.example.contactimporter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.net.URISyntaxException;

public class FileUtils {

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public  static Boolean isExcelFile(String filename){
        String extension = filename.substring(filename.lastIndexOf('.')+1);
        return extension.equals("xls") || extension.equals("xlsx");
    }

}
