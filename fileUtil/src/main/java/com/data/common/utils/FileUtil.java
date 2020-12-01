package com.data.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    /**
     * @param fileName - The file's name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param path     - The file's path.
     * @param content  - The file's content to flush into the file.
     * @throws IOException
     */
    public static void writeFile(String fileName, String path, String content) throws IOException {
        writeFile(path + "\\" + fileName, content.getBytes());
    }

    /**
     * @param path    - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param content - The file's content to flush into the file.
     * @throws IOException
     */
    public static void writeFile(String path, String content) throws IOException {
        writeFile(path, content.getBytes());
    }

    /**
     * @param path  - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param bytes - The file's bytes to flush into the file.
     * @throws IOException
     */
    public static void writeFile(String path, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(bytes);
        fos.close();
    }

    /**
     * Get the file's content.
     *
     * @param fileName - The file's name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param path     - The file's path.
     * @return A string with the file's content.
     * @throws IOException
     */
    public static String readFile(String fileName, String path) throws IOException {
        return readFile(path + "\\" + fileName);
    }

    /**
     * Get the file's content.
     *
     * @param path - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @return A string with the file's content.
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        StringBuilder data = new StringBuilder();
        int c;
        while ((c = fis.read()) != -1) {
            data.append((char) c);
        }
        fis.close();
        return data.toString();
    }

    /**
     * Check if the file contains a certain content or not.
     *
     * @param path    - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param content - The content to check if the file contains it...
     * @return A boolean value whether the file contains the content or not.
     * @throws IOException
     */
    public static boolean isFileContainsContent(String path, String content) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        StringBuilder data = new StringBuilder();
        int c;
        while ((c = fis.read()) != -1) {
            data.append((char) c);
        }

        return data.toString().contains(content);
    }

    /**
     * Deletes a certain file.
     *
     * @param path - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @throws IOException
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    /**
     * Wipes a file's content.
     *
     * @param path - The file's path + file name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @throws IOException
     */
    public static void wipeFile(String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(("").getBytes());
        fos.close();
    }

    /**
     * Wipes all files content in a folder.
     *
     * @param path - The folder's path.
     * @throws IOException
     */
    public static void wipeFolder(String path) throws IOException {
        for (File file : scanFolder(path)) {
            wipeFile(file.getPath() + file.getName());
        }
    }

    /**
     * Deletes all files in a folder.
     *
     * @param path - The folder's path.
     * @throws IOException
     */
    public static void clearFolder(String path) {
        for (File file : scanFolder(path)) {
            file.delete();
        }
    }

    /**
     * Returns an array of files from a requested folder.
     *
     * @param path - The folder's path.
     * @return An array of File (@see java.io.File (Object)).
     * @throws IOException
     */
    public static File[] scanFolder(String path) {
        return new File(path).listFiles();
    }

    /**
     * Creates a new file.
     *
     * @param fileName - The file's name (MAKE SURE THE FILE ENDS WITH THE EXTENSION).
     * @param path     - The file's path.
     * @throws IOException
     */
    public static void createFile(String fileName, String path) throws IOException {
        File file = new File(path + "\\" + fileName);
        file.createNewFile();
    }

}
