package edu.northwestern.bioinformatics.studycalendar.xml.validators;

import java.net.URL;
import java.io.File;

public enum Schema {
    activities("Activities", "activities.xsd"), template("Template", "template.xsd");

    private String title;
    private String fileName;

    private Schema(String title, String fileName) {
        this.title = title;
        this.fileName = fileName;
    }

    public String title() {
        return title;
    }

    public String fileName() {
        return fileName;
    }

    public File file() {
        URL schemaLocation = getClass().getClassLoader().getResource(fileName);
        return new File(schemaLocation.getFile());
    }
}
