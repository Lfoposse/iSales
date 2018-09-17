package com.rainbow_cl.i_sales.remote.model;

/**
 * Created by netserve on 30/08/2018.
 */

public class DolPhoto {
    private String filename;
    private String content;
    private String encoding;

    public DolPhoto() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
