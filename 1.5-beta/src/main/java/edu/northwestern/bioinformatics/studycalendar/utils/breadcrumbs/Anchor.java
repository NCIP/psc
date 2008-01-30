package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

/**
 * @author Rhett Sutphin
 */
public class Anchor {
    private String url;
    private String text;

    public Anchor(String url, String text) {
        this.url = url;
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return new StringBuilder()
            .append(url).append(" : \"").append(text).append('"').toString();
    }
}
