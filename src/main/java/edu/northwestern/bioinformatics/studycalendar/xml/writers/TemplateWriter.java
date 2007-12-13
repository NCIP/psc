package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TemplateWriter {
    public static String ROOT_START =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<study xmlns=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\"\n" +
             "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
             "     xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\">\n";

    public static String PLANNDED_CALENDAR = "<planned-calendar/>";
    public static String AMENDMENT_START = "<amendment>";
    public static String DELTA_START = "<delta>";
    public static String ADD = "<add/>";

    public static String DELTA_END = "</delta>";
    public static String AMENDMENT_END = "</amendment>";
    public static String ROOT_END = "</study>";

    public byte[] writeTemplate() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(os);
        p.println(ROOT_START);
        p.println(PLANNDED_CALENDAR);
        p.println(AMENDMENT_START);
        p.println(DELTA_START);
        p.println(ADD);
        p.println(DELTA_END);
        p.println(AMENDMENT_END);
        p.println(ROOT_END);

        return os.toByteArray();
    }

    public byte[] writeTemplate(Study study) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(os);

        p.println(ROOT_START);
        p.println(PLANNDED_CALENDAR);

        p.println(ROOT_END);

        return os.toByteArray();
    }
}
