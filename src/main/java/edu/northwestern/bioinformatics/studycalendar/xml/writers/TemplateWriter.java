package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class TemplateWriter {
    public static String ROOT_START =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<study xmlns=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\"\n" +
             "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
             "     xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\">\n";

    public static String PLANNDED_CALENDAR = "<planned-calendar/>";
    public static String AMENDMENT_START = "<amendment>";
    public static String DELTA_START = "<delta>";
    public static String ADD_CHANGE = "<add/>";

    public static String DELTA_END = "</delta>";
    public static String AMENDMENT_END = "</amendment>";
    public static String ROOT_END = "</study>";

    public byte[] writeTemplate(Study study) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(os);

        p.println(ROOT_START);
        p.println(PLANNDED_CALENDAR);

        List<Amendment> amendments = study.getAmendmentsList();
        for (Amendment amendment : amendments) {
            p.println(AMENDMENT_START);

            writeDeltas(p, amendment);

            p.println(AMENDMENT_END);
        }

        p.println(ROOT_END);

        return os.toByteArray();
    }

    private void writeDeltas(PrintStream p, Amendment amendment) {
        for (Delta<?> delta :  amendment.getDeltas()) {
            p.println(DELTA_START);

            writeChanges(p, delta);

            p.println(DELTA_END);
        }
    }

    private void writeChanges(PrintStream p, Delta<?> delta) {
        for (Change change : delta.getChanges()) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                p.println(ADD_CHANGE);
            }
        }
    }
}
