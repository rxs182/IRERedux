package com.sherwin.ireredux;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SurfaceMask {
    @JacksonXmlProperty(isAttribute = true)
    private String color;
    @JacksonXmlProperty(isAttribute = true)
    private int version;
    @JacksonXmlProperty(isAttribute = true)
    private String string;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
