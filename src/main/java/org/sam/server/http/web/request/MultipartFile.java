package org.sam.server.http.web.request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * multipart/form-data 요청으로 온 파일을 저장하는 클래스
 *
 * @author hypernova1
 * @see HttpMultipartRequest
 */
public class MultipartFile {

    private final String fileName;

    private final String mimeType;

    private final byte[] fileData;

    public MultipartFile(String fileName, String mimeType, byte[] fileData) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileData = fileData;
    }

    /**
     * 인자로 받은 경로에 파일을 저장한다.
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
     * 파일의 미디어 타입을 반환한다.
     *
     * @return 미디어 타입
     * */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일의 이름을 반환한다.
     *
     * @return 파일 이름
     * */
    public String getFileName() {
        return fileName;
    }

}
