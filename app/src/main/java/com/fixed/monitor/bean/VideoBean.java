package com.fixed.monitor.bean;

/**
 * @param
 * @return
 * @description 
 * @author jiejack
 * @time 2022/2/2 9:37 下午
 */
public class VideoBean {
    private String title;
    private String url;
    private String thumb;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public VideoBean(String title, String thumb, String url) {
        this.title = title;
        this.url = url;
        this.thumb = thumb;
    }
}
