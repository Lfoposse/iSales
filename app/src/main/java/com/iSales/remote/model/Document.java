package com.iSales.remote.model;

/**
 * Created by netserve on 06/10/2018.
 */

public class Document {
    private String filename;
    private String modulepart;
    private String ref;
    private String subdir;
    private String filecontent;
    private String fileencoding;
    private String overwriteifexists;

    public Document() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getModulepart() {
        return modulepart;
    }

    public void setModulepart(String modulepart) {
        this.modulepart = modulepart;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSubdir() {
        return subdir;
    }

    public void setSubdir(String subdir) {
        this.subdir = subdir;
    }

    public String getFilecontent() {
        return filecontent;
    }

    public void setFilecontent(String filecontent) {
        this.filecontent = filecontent;
    }

    public String getFileencoding() {
        return fileencoding;
    }

    public void setFileencoding(String fileencoding) {
        this.fileencoding = fileencoding;
    }

    public String getOverwriteifexists() {
        return overwriteifexists;
    }

    public void setOverwriteifexists(String overwriteifexists) {
        this.overwriteifexists = overwriteifexists;
    }
}
