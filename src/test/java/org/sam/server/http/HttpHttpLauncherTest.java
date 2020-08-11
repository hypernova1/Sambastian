package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 3:47 PM
 */
class HttpHttpLauncherTest {

    @Test
    void test() {
        String handlerPath = "/";
        String requestPath = "/path";

        assertEquals(true, requestPath.startsWith(handlerPath));
    }

    @Test
    void test2() throws IOException {
        String formData = "------WebKitFormBoundarybN8S4aB20v24VBLR\n" +
                "Content-Disposition: form-data; name=\"name\"\n" +
                "\n" +
                "sam\n" +
                "------WebKitFormBoundarybN8S4aB20v24VBLR\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"component.html\"\n" +
                "Content-Type: text/html\n" +
                "\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"kr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "    <title>Document</title>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/vue/dist/vue.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <todo-item></todo-item>\n" +
                "<script>\n" +
                "    Vue.component('todo-item', {\n" +
                "        template: '<ol>확인</ol>'\n" +
                "    })    \n" +
                "</script>\n" +
                "</body>\n" +
                "</html>\n" +
                "------WebKitFormBoundarybN8S4aB20v24VBLR--";

        String[] rawFormDataList = formData.replace("/\\s/g", "").split("------WebKitFormBoundarybN8S4aB20v24VBLR");
        List<String> multipartList = Arrays.asList(rawFormDataList);
        multipartList = multipartList.subList(1, multipartList.size() - 1);

        multipartList.forEach(multipartText -> {
            Pattern pattern = Pattern.compile("\\\"(.*?)\\\"");
            String name;
            int doubleNewLineIndex = multipartText.indexOf("\n\n");
            String fileInfo = multipartText.substring(0, doubleNewLineIndex).replaceAll("^\\s+","");
            int isFileData = fileInfo.indexOf("\n");
            if (isFileData == -1) {
                name = fileInfo.split("; ")[1];
                String value = multipartText.substring(doubleNewLineIndex).trim();
            } else {
                String[] fileInfoArr = fileInfo.split("\n");
                name = fileInfoArr[0].split("; ")[1];
                String contentType = fileInfoArr[1].split(": ")[1];
                String fileData = multipartText.substring(doubleNewLineIndex).replaceAll("^\\s+", "");
                String fileName = fileInfoArr[0].split("; ")[2];
                Matcher matcher = pattern.matcher(fileName);
                while(matcher.find()) {
                    fileName = matcher.group().replace("\"", "");
                }
            }
            Matcher matcher = pattern.matcher(name);
            while(matcher.find()) {
                name = matcher.group().replace("\"", "");
            }
        });


        String text = multipartList.get(0);
        int doubleNewLineIndex = text.indexOf("\n\n");

        String fileInfo = text.substring(0, doubleNewLineIndex);
        int isFileData = fileInfo.replaceAll("^\\s+","").indexOf("\n");


        String fileData = multipartList.get(1).replaceAll("^\\s+", "");
        int index = fileData.indexOf("\n\n");
        String substring2 = fileData.substring(0, index);
        String substring = fileData.substring(index).replaceAll("^\\s+", "");
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/melchor/test.txt");
        fileOutputStream.write(substring.getBytes());


        List<List<String>> multipartFormDataList = multipartList.stream().map(multipart -> {
            List<String> lines = Arrays.asList(multipart.split("\\n\\n"));
            lines = lines.stream().filter(line -> !line.isEmpty()).collect(Collectors.toList());
            return lines;
        }).collect(Collectors.toList());

        Pattern pattern = Pattern.compile("\\\"(.*?)\\\"");

        multipartFormDataList.forEach(multipartFormData -> {
            String[] descriptions = multipartFormData.get(0).trim().split("\\n");

            String name = descriptions[0].split("; ")[1];
            Matcher matcher = pattern.matcher(name);
            while(matcher.find()) {
                name = matcher.group().replace("\"", "");
            }
            if (descriptions.length == 1) {
                String data = multipartFormData.get(1).trim();
            } else {
                String fileName = descriptions[0].split("; ")[2];
                matcher = pattern.matcher(fileName);
                while (matcher.find()) fileName = matcher.group().replace("\"", "");
                String contentType = descriptions[1].split(": ")[1];
                String file = multipartFormData.get(1);
            }

        });
    }

    @Test
    void split() {
        String str = "Content-Disposition: form-data; name=\"file\"; filename=\"Untitled2.rtf\"";
        String[] split = str.split("\"");
        System.out.println(split.length);
        System.out.println(split[1]);
        System.out.println(split[3]);
    }

    @Test
    void compare_array() {
        byte[] arr = {-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, -1, -37, 0, -124, 0, 10, 8, 9, 10, 9, 8, 10, 10, 9, 10, 18, 21, 10, 12, 13, 28, 26, 13, 13, 13, 21, 16, 18, 19, 24, 20, 28, 24, 23, 17, 27, 24, 18, 26, 27, 23, 50, 26, 29, 31, 46, 32, 14, 26, 30, 48, 33, 27, 30, 55, 43, 39, 47, 29, 23, 49, 56, 49, 50, 40, 28, 32, 45, 31, 1, 11, 11, 11, 13, 5, 5, 18, 5, 14, 10, 32, 21, 14, 18, 37, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 31, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, -1, -64, 0, 17, 8, 0, -31, 0, -31, 3, 1, 34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 26, 0, 1, 1, 1, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 7, 2, 5, 6, 3, -1, -60, 0, 36, 16, 1, 0, 1, 4, 1, 4, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 17, 18, 5, 33, 49, 65, 7, 6, 19, 97, -127, -111, -1, -60, 0, 21, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -60, 0, 21, 17, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -58, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 93, 28, 100, 16, 93, 26, 4, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 116, -27, 76, 68, 119, -107, 29, -113, 68, -23, -43, 117, 94, -85, -123, -127, 77, 92, 103, 42, -3, 52, 114, -98, -6, -119, -98, -13, -81, 122, -115, -73, 124, -33, -115, 62, -98, -85, -91, 87, -113, -115, -113, 85, 25, 52, -47, 63, 111, 43, -99, 115, 95, 45, 121, -81, -65, 25, -115, -6, -45, 1, -59, -53, -71, -119, 126, -42, 70, 61, 115, 77, -21, 55, 34, -85, 119, 35, -52, 85, 19, -54, 39, -6, -9, -3, 67, -27, 78, -81, -103, -46, -22, -60, -93, 22, -51, -69, -9, 45, -15, -69, -109, 110, 107, -103, -44, -57, 25, -102, 41, -97, 19, -5, -111, 43, 59, -71, 17, 76, -52, 79, -104, -19, 58, -4, 75, -29, 50, 76, -6, -11, 30, 28, 81, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 104, 0, -90, -48, 5, -108, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 127, -1, -39, 13, 10, 45, 45, 45, 45, 45, 45, 87, 101, 98, 75, 105, 116, 70, 111, 114, 109, 66, 111, 117, 110, 100, 97, 114, 121, 106, 66, 86, 66, 78, 110, 51, 108, 116, 57, 71, 66, 90, 108, 115, 65, 45, 45, 13, 10};
        byte[] arr2 = {45, 45, 45, 45, 45, 45, 87, 101, 98, 75, 105, 116, 70, 111, 114, 109, 66, 111, 117, 110, 100, 97, 114, 121, 106, 66, 86, 66, 78, 110, 51, 108, 116, 57, 71, 66, 90, 108, 115, 65};

        String content = new String(arr, StandardCharsets.UTF_8);
        String content2 = new String(arr2, StandardCharsets.UTF_8);
        int index = content.indexOf(content2);
        assertTrue(index != -1);
    }

}