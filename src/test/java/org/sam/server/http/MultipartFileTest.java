package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFileTest {

    @Test
    void writeFile() throws IOException {
        InputStream fis = new FileInputStream("/Users/melchor/Downloads/download.jpeg");

        int i;
        File file = new File("/Users/melchor/download.jpeg");
        FileOutputStream fos = new FileOutputStream(file);
        while ((i = fis.read()) != -1) {
            fos.write(i);
            System.out.println(i);
        }
        fos.flush();
        fos.close();
    }

}