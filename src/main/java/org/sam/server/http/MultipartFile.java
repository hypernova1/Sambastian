package org.sam.server.http;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        byte[] bytes = fileData.getBytes();
        ByteArrayInputStream fis = new ByteArrayInputStream(bytes);
        for (byte b : bytes) {
            System.out.println(b);
        }
//        int i;
//        File file = new File(path);
//        FileOutputStream fos = new FileOutputStream(file);
//        while ((i = fis.read()) != -1) {
//            fos.write(i);
//        }
//        fos.flush();
//        fos.close();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
