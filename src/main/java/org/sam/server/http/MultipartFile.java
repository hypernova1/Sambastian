package org.sam.server.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by melchor
 * Date: 2020/07/28
 * Time: 11:16 PM
 */
public class MultipartFile {

    private final String fileName;
    private final String contentType;
    private final byte[] fileData;

    protected MultipartFile(String fileName, String contentType, byte[] fileData) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
    }

    public void saveTo(String path) throws IOException {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData);
        fos.flush();
        fos.close();
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

}
