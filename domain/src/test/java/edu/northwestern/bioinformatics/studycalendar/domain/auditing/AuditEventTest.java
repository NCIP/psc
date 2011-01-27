package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

import org.hibernate.type.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNegative;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;

/**
 * @author Jalpa Patel
 */
public class AuditEventTest extends DomainTestCase {
    private AuditEvent ae0;
    private final String STUDY_URL = "/psc/pages/newStudy";
    private final String NAME = "name";
    private final String ID = "id";
    private SimpleDateFormat sdf = DateFormat.getUTCFormat();

    protected void setUp() throws Exception {
        super.setUp();
        Study study =  new Study();
        ae0 = new AuditEvent(study, Operation.UPDATE, DataAuditInfo.copy(createAuditInfoFor(STUDY_URL)));
   }

    public void testAppendEventValuesForName() throws Exception {
        DataAuditEventValue expectedValue = new DataAuditEventValue(NAME, null, "testStudy");
        ae0.appendEventValues(new StringType(), NAME, null, "testStudy");
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForObjectId() throws Exception {
        Study study = new Study();
        study.setId(12);
        DataAuditEventValue expectedValue = new DataAuditEventValue(ID, null, study.getId().toString());
        ae0.appendEventValues(new StringType(), ID, null, study);
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForString() throws Exception {
        List<String> labels = new ArrayList<String>();
        labels.add("label1");
        labels.add("label2");
        labels.add("label3");
        DataAuditEventValue expectedValue = new DataAuditEventValue("label", null, "[label1, label2, label3]");
        ae0.appendEventValues(new StringType(), "label", null, labels);
        assertNotNull(ae0.getValues());
        DataAuditEventValue actualValue = ae0.getValues().get(0);
        assertEquals("Property name is different", expectedValue.getAttributeName(), actualValue.getAttributeName());
        assertEquals("Previous value is different", expectedValue.getPreviousValue(), actualValue.getPreviousValue());
        assertEquals("Current value is different", expectedValue.getCurrentValue(), actualValue.getCurrentValue());
    }

    public void testAppendEventValuesForCollectionType() throws Exception {
        ae0.appendEventValues(new SetType("plannedActivity", null, false), "label", null, new ArrayList<String>());
        assertEquals("Event value should not be added for collection type", 0, ae0.getValues().size());
    }

    public void testAppendEventValuesForHibenateBackrefProperty() throws Exception {
        ae0.appendEventValues(new AnyType(), "label-Backref", null, null);
        assertEquals("Event value should not be added for hibernate Backref property", 0, ae0.getValues().size());
    }

    public void testAppendEventValuesForPreviousAndCurrentStateNull() throws Exception {
        ae0.appendEventValues(new StringType(), NAME, null, null);
        assertEquals("Event value should not be added for null for both previous and current state", 0, ae0.getValues().size());
    }

    public void testAppendEventValueIfPreviousAndCurrentStateAreEquals() throws Exception {
        ae0.appendEventValues(new StringType(), NAME, "testName", "testName");
        assertEquals("Event value should not be added when previous and current state are equal", 0, ae0.getValues().size());
    }

    public void testReverseChronologicalOrder() throws Exception {
        AuditEvent ae1 = new AuditEvent(new Study(), Operation.UPDATE,
                new DataAuditInfo("User", "10.10.10.10", sdf.parse("2010-08-17 10:40:58.361"), STUDY_URL));
        AuditEvent ae2 = new AuditEvent(new Study(), Operation.UPDATE,
                new DataAuditInfo("User", "10.10.10.10", sdf.parse("2010-08-17 10:41:58.361"), STUDY_URL));

        assertPositive(ae1.compareTo(ae2));
        assertNegative(ae2.compareTo(ae1));
    }

    //Test Helper methods
    private DataAuditInfo createAuditInfoFor(String url) {
        return new DataAuditInfo("userName", "10.10.10.155", new Date(), url);
    }
}
