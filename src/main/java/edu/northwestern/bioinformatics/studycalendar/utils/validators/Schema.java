package edu.northwestern.bioinformatics.studycalendar.utils.validators;

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
}
