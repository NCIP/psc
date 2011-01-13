package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 *@author  Saurabh Agrawal.
 */
public class PeriodDeltaXmlValidationTest extends AbstractXmlValidationTestCase {
    private Element ePeriod ;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ePeriod=periodDeltaXmlSerializer.createElement(periodDelta);
    }

    public void testValidteElement() {
        amendment.getDeltas().clear();
        assertFalse(StringUtils.isBlank(periodDeltaXmlSerializer.validate(amendment, ePeriod)));
        assertEquals(String.format("\n released amendment present in the system does have  any delta matching with provied grid id %s and node id  %s of delta.\n",
                periodDelta.getGridId(), period3.getGridId()), periodDeltaXmlSerializer.validate(amendment, ePeriod).toString());

        amendment.addDelta(periodDelta);
        assertTrue(StringUtils.isBlank(periodDeltaXmlSerializer.validate(amendment, ePeriod)));

    }


    public void testValidateElementForDifferentNumberOfChanges() {

        periodDelta.addChange(add1);
        amendment.addDelta(periodDelta);
        assertEquals(String.format("Imported document has different number of Changes for  delta (id :%s).  Please make sure changes are identical and they are in same order." , periodDelta.getGridId())
                , periodDeltaXmlSerializer.validate(amendment, ePeriod).toString());


    }

    public void testValidateForInValidAddChangeContent() throws IOException, SAXException {
        periodDelta.addChange(add1);
        amendment.addDelta(periodDelta);
        ePeriod = periodDeltaXmlSerializer.createElement(periodDelta);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setActivity(activity3);

        assertTrue(periodDeltaXmlSerializer.validate(amendment, ePeriod).contains("activity references are different for PlannedActivity"));


    }

    public void testValidateForInValidPropertyChangeAttributes() throws IOException, SAXException {

        periodDelta.addChange(add1);
        amendment.addDelta(periodDelta);
        ePeriod = periodDeltaXmlSerializer.createElement(periodDelta);


        add1.setIndex(5);


        assertTrue(periodDeltaXmlSerializer.validate(amendment, ePeriod).contains("index is different. expected:5 , found (in imported document) :0"));


    }

    public void testValidateForIdenticalDelta() throws IOException, SAXException {

        periodDelta.addChange(add1);
        amendment.addDelta(periodDelta);
        ePeriod = periodDeltaXmlSerializer.createElement(periodDelta);


        assertTrue(StringUtils.isBlank(periodDeltaXmlSerializer.validate(amendment, ePeriod)));


    }

}
