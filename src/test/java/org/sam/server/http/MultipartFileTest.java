package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by melchor
 * Date: 2020/08/03
 * Time: 9:48 AM
 */
class MultipartFileTest {

    @Test
    void writeFile() throws IOException {
        InputStream fis = new FileInputStream("/Users/melchor/Downloads/보안카드.jpeg");
//        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//
//        FileWriter writer = new FileWriter(new File("/Users/melchor/download.jpeg"));
//        BufferedWriter bw = new BufferedWriter(writer);
//        int j ;
//        while ((j = br.read()) != -1) {
//            bw.write(j);
//            System.out.println(j);
//        }
//        bw.flush();
//        bw.close();

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