package org.sam.server.common;

public class ArrayUtils {
    /**
     * 입력받은 배열의 길이의 두배인 배열을 생성하고 카피 후 반환합니다.
     *
     * @param data 배열
     * @return 2배 길이의 배열
     */
    public static byte[] getDoubleArray(byte[] data) {
        byte[] arr = new byte[data.length * 2];
        System.arraycopy(data, 0, arr, 0, data.length);
        return arr;
    }

    /**
     * 배열의 길이가 최대인지 확인합니다.
     *
     * @param data       확인할 배열
     * @param fileLength 파일 길이
     * @return 배열의 길이가 최대인지 여부
     */
    public static boolean isFullCapacity(byte[] data, int fileLength) {
        return data.length == fileLength;
    }
}
