package org.sam.server.http.web;

import org.sam.server.http.web.HttpMultipartRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * multipart/form-data 요청으로 온 파일을 저장하는 클래스입니다.
 *
 * @author hypernova1
 * @see HttpMultipartRequest
 */
public class MultipartFile {

    private final String fileName;

    private final String contentType;

    private final byte[] fileData;

    public MultipartFile(String fileName, String contentType, byte[] fileData) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
    }

    /**
     * 인자로 받은 경로에 파일을 저장합니다.
     *
     * @param path 파일을 저장할 위치
     * @throws IOException 파일을 쓰다가 오류 발생시
     * */
    public void saveTo(String path) throws IOException {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData);
        fos.flush();
        fos.close();
    }

    /**
     * 파일의 미디어 타입을 반환합니다.
     *
     * @return 미디어 타입
     * @see org.sam.server.constant.ContentType
     * */
    public String getContentType() {
        return contentType;
    }

    /**
     * 파일의 이름을 반환합니다.
     *
     * @return 파일 이름
     * */
    public String getFileName() {
        return fileName;
    }

}
