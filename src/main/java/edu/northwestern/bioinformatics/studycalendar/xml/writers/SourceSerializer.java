package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractCsvXlsSerializer;


public class SourceSerializer implements AbstractCsvXlsSerializer<Source> {

    private final static String ACTIVITY_NAME ="Name";
    private final static String ACTIVITY_TYPE ="Type";
    private final static String ACTIVITY_CODE ="Code";
    private final static String ACTIVITY_DESCRIPTION ="Description";
    private final static String ACTIVITY_SOURCE ="Source";
    private final static String EMPTY_STRING ="";

    private final String NEW_STRING = "\n";
    private String[] arrayOfHeaders = new String[] {ACTIVITY_NAME, ACTIVITY_TYPE, ACTIVITY_CODE, ACTIVITY_DESCRIPTION, ACTIVITY_SOURCE};

    public String createDocumentString(Source source, String delimeter){
        StringBuffer sb = new StringBuffer();
        sb.append(constructHeader(delimeter));
        for (Activity a : source.getActivities()) {
            sb.append(a.getName());
            sb.append(delimeter);
            sb.append(a.getType().getName());
            sb.append(delimeter);
            sb.append((a.getCode() == null || (EMPTY_STRING).equals(a.getCode()) ? EMPTY_STRING : a.getCode()));
            sb.append(delimeter);
            sb.append((a.getDescription() ==null || (EMPTY_STRING).equals(a.getDescription()) ? EMPTY_STRING : a.getDescription()));
            sb.append(delimeter);
            sb.append(source.getName());
            sb.append(NEW_STRING);
        }
        return sb.toString();
    }


    public String constructHeader(String delimiter){
        StringBuffer sb = new StringBuffer();
        for (String header: arrayOfHeaders) {
            sb.append(header);
            sb.append(delimiter);
        }
        sb.append(NEW_STRING);
        return sb.toString();
    }
}
