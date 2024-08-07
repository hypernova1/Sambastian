package org.sam.server.bean;

/**
 * 요청된 정적 자원을 찾을 수 없을 시 발생한다.
 *
 * @author hypernova1
 * */
public class ResourcesNotFoundException extends RuntimeException {
    public ResourcesNotFoundException(String filename) {
        super(filename + " not found.");
    }
}
