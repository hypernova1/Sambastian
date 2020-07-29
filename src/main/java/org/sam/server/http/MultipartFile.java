package org.sam.server.http;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by melchor
 * Date: 2020/07/28
 * Time: 11:16 PM
 */
public class MultipartFile {

    private final String name;
    private final String type;
    private final String fileData;

    protected MultipartFile(String name, String type, String fileData) {
        this.name = name;
        this.type = type;
        this.fileData = fileData;
    }

    public void saveTo(String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        outputStream.write(fileData.getBytes());
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
