/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 *@author  Saurabh Agrawal.
 */
public class StudySegmentDeltaXmlValidationTest extends AbstractXmlValidationTestCase {
    private Element eStudySegment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eStudySegment =studySegmentDeltaXmlSerializer.createElement(studySegmentDelta);
    }

    public void testValidteElement() {
        amendment.getDeltas().clear();
        assertFalse(StringUtils.isBlank(studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment)));
        assertEquals(String.format("\n released amendment present in the system does have  any delta matching with provied grid id %s and node id  %s of delta.\n",
                studySegmentDelta.getGridId(), studySegment1.getGridId()), studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment).toString());

        amendment.addDelta(studySegmentDelta);
        assertTrue(StringUtils.isBlank(studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment)));

    }


    public void testValidateElementForDifferentNumberOfChanges() {

        studySegmentDelta.addChange(add1);
        amendment.addDelta(studySegmentDelta);
        assertEquals(String.format("Imported document has different number of Changes for  delta (id :%s).  Please make sure changes are identical and they are in same order." , studySegmentDelta.getGridId())
                , studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment).toString());


    }

    public void testValidateForInValidAddChangeContent() throws IOException, SAXException {
        studySegmentDelta.addChange(add1);
        amendment.addDelta(studySegmentDelta);
        eStudySegment = studySegmentDeltaXmlSerializer.createElement(studySegmentDelta);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setActivity(activity3);

        assertTrue(studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment).contains("activity references are different for PlannedActivity"));


    }

    public void testValidateForInValidPropertyChangeAttributes() throws IOException, SAXException {

        studySegmentDelta.addChange(add1);
        amendment.addDelta(studySegmentDelta);
        eStudySegment = studySegmentDeltaXmlSerializer.createElement(studySegmentDelta);


        add1.setIndex(5);


        assertTrue(studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment).contains("index is different. expected:5 , found (in imported document) :0"));


    }

    public void testValidateForIdenticalDelta() throws IOException, SAXException {

        studySegmentDelta.addChange(add1);
        amendment.addDelta(studySegmentDelta);
        eStudySegment = studySegmentDeltaXmlSerializer.createElement(studySegmentDelta);


        assertTrue(StringUtils.isBlank(studySegmentDeltaXmlSerializer.validate(amendment, eStudySegment)));


    }

}