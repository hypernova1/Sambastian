package org.sam.server.http;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;

/**
 *  응답에 대한 가능을 정의 해놓은 인터페이스입니다.
 *
 * @author hypernova1
 * @see HttpResponse
 * */
public interface Response {

    /**
     * HTTP 응답 메시지를 만듭니다.
     *
     * @param pathOrJson 파일 경로 or JSON
     * @param status 응답 HttpStatus
     * */
    void execute(String pathOrJson, HttpStatus status);

    void responseFavicon();

    void responseStaticResources();

    void notFound();

    void badRequest();

    void methodNotAllowed();

    void setHeader(String key, String value);

    void setContentMimeType(ContentType contentMimeType);

    void executeOptionsResponse();

    void responseIndexFile();

    void addAllowedMethod(HttpMethod httpMethod);
}
