package org.sam.server.http;

/**
 * Created by melchor
 * Date: 2020/07/28
 * Time: 11:16 PM
 */
public class File {

    private String name;
    private String type;
    private String fileData;

    protected File(String name, String type, String fileData) {
        this.name = name;
        this.type = type;
        this.fileData = fileData;
    }

    public void saveTo(String path) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
