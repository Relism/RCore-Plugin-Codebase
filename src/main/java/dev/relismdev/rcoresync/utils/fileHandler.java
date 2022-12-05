package dev.relismdev.rcoresync.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class fileHandler {

    public static void downloadFile(URL url, File destinationFile) {
        try {
            // Open a connection to the URL and get an input stream for reading the file
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            // Create the destination file and a stream to write to it
            destinationFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            // Read bytes from the input stream and write them to the destination file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            // Close the streams
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            msg.log("&cCould not download file: &d" + e.getMessage());
        }
    }
}
