package com.sherwin.ireredux;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Surface {
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    @JacksonXmlProperty(isAttribute = true)
    private String region;
    @JacksonXmlProperty(isAttribute = true)
    private int width;
    @JacksonXmlProperty(isAttribute = true)
    private int height;
    @JacksonXmlProperty(isAttribute = true)
    private int version;
    @JacksonXmlProperty(isAttribute = true)
    private boolean autoQuad;
    @JacksonXmlProperty(isAttribute = true)
    private String category;
    @JacksonXmlProperty(isAttribute = true)
    private int sensitivity;
    @JacksonXmlProperty(isAttribute = true)
    private int intensity;
    @JacksonXmlProperty(isAttribute = true)
    private int thickness;
    @JacksonXmlProperty(isAttribute = true)
    private int brightness;
    @JacksonXmlProperty(isAttribute = true)
    private int rotate;
    @JacksonXmlProperty(isAttribute = true)
    private int gridLength;
    @JacksonXmlProperty(isAttribute = true)
    private int gridWidth;
    @JacksonXmlProperty(isAttribute = true)
    private int maxY;
    @JacksonXmlProperty(isAttribute = true)
    private int minY;
    @JacksonXmlProperty(isAttribute = true)
    private int maxX;
    @JacksonXmlProperty(isAttribute = true)
    private int minX;
    @JacksonXmlProperty(isAttribute = true)
    private String quad;
    @JacksonXmlProperty(isAttribute = true)
    private String asset;
    // There should only be one surfacemask
    @JsonProperty("SurfaceMask")
    private List<SurfaceMask> surfaceMask;
    @JsonProperty("SurfaceOverlay")
    private SurfaceOverlay surfaceOverlay;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public boolean isAutoQuad() {
        return autoQuad;
    }

    public void setAutoQuad(boolean autoQuad) {
        this.autoQuad = autoQuad;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public int getGridLength() {
        return gridLength;
    }

    public void setGridLength(int gridLength) {
        this.gridLength = gridLength;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public String getQuad() {
        return quad;
    }

    public void setQuad(String quad) {
        this.quad = quad;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public List<SurfaceMask> getSurfaceMask() {
        return surfaceMask;
    }

    public void setSurfaceMask(List<SurfaceMask> surfaceMask) {
        this.surfaceMask = surfaceMask;
    }

    public SurfaceOverlay getSurfaceOverlay() {
        return surfaceOverlay;
    }

    public void setSurfaceOverlay(SurfaceOverlay surfaceOverlay) {
        this.surfaceOverlay = surfaceOverlay;
    }
}
