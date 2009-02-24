package edu.northwestern.bioinformatics.studycalendar.xml.validators;

import java.net.URL;
import java.io.File;

public enum Schema {
    activities("Activities", "psc.xsd"), template("Template", "psc.xsd");

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
        URL schemaLocation = url();
        return new File(schemaLocation.getFile());
    }

    protected URL url() {
        return getClass().getClassLoader().getResource(fileName);
    }


}
