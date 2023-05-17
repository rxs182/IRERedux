package com.sherwin.ireredux;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public final class Project {
    @JacksonXmlProperty(isAttribute = true)
    private String image;
    @JacksonXmlProperty(isAttribute = true)
    private int width;
    @JacksonXmlProperty(isAttribute = true)
    private int height;
    @JacksonXmlProperty(isAttribute = true)
    private int version;
    @JacksonXmlProperty(isAttribute = true)
    private boolean empty;
    private List<Surface> surfaces = new ArrayList<>();
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public List<Surface> getSurfaces() {
        return surfaces;
    }

    public void setSurfaces(List<Surface> surfaces) {
        this.surfaces = surfaces;
    }
}
