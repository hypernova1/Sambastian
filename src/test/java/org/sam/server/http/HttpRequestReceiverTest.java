package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 3:47 PM
 */
class HttpRequestReceiverTest {

    @Test
    void test() {
        String handlerPath = "/";
        String requestPath = "/path";

        assertEquals(true, requestPath.startsWith(handlerPath));
    }

    @Test
    void test2() {
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

}