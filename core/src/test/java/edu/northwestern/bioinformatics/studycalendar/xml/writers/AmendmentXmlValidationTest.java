/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class AmendmentXmlValidationTest extends AbstractXmlValidationTestCase {

    private Element eDelta;
    private Element eAmendment;

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        epochDelta = new EpochDelta(epoch1);
        epochDelta.setGridId("6b2d06a3-f521-4ef8-9cae-3eb73f6f6bf4");
        epochDelta.addChange(add1);


        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

        eAmendment = amendmentSerializer.createElement(amendment);
    }

    public void testValidDevelopAmendment() throws IOException, SAXException {

        String message = developmentAmendmentSerializer.validateDevelopmentAmendment(eAmendment);
        assertTrue(StringUtils.isBlank(message));


    }

    public void testValidIdenticalRelsasedAmendment() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        amendment.getDeltas().add(periodDelta);
        amendment.getDeltas().add(studySegmentDelta);
        amendment.getDeltas().add(epochDelta);

        eAmendment = amendmentSerializer.createElement(amendment);
        study.setAmendment(amendment);
        String message = amendmentSerializer.validate(amendment, eAmendment);
        assertEquals("", message);
        assertTrue(StringUtils.isBlank(message));


    }

    public void testInvalidReleasedAmendment() throws IOException, SAXException {
        String message = amendmentSerializer.validate(firstAmendment, eAmendment);
        assertTrue(message.contains(String.format("A released amendment %s present in the system is not present in the imported document. ",
                firstAmendment.getDisplayName())));


    }


    public void testValidateIfDevelopmentAmendmentMatchesWithReleasedAmendment() throws IOException, SAXException {
        study.setAmendment(amendment);
        String message = developmentAmendmentSerializer.validateDevelopmentAmendment(eAmendment);
        assertTrue(message.contains("Imported document must not have any development amendment which matches with any relased amendment present in system"));


    }


    public void testValidateForInValidDeltas() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        String message = amendmentSerializer.validate(amendment, eAmendment);
        assertTrue(message.contains(String.format("Imported document and release amendment %s present in system must have identical number of deltas",
                amendment.getDisplayName())));

    }

    public void testValidateForDifferentChangesAttributes() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);
        add1.setGridId("wrong grid id");

        String message = amendmentSerializer.validate(amendment, eAmendment);
        assertTrue(message.contains("grid id is different. expected:wrong grid id , found (in imported document) :cb6e3130-9d2e-44e8-80ac-170d1875db5c"));


    }

    public void testValidateForDifferentReorderChange() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);


        ((Epoch) reorder.getChild()).setName("wrong name");

        String message = amendmentSerializer.validate(amendment, eAmendment);

        assertTrue(message.contains("name  is different for Epoch. expected:wrong name , found (in imported document) :Treatment"));


    }

    public void testValidateForDifferentChangeContent() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        ((Epoch) add1.getChild()).setName("wrong name");


        String message = amendmentSerializer.validate(amendment, eAmendment);

        assertTrue(message.contains("name  is different for Epoch. expected:wrong name , found (in imported document) :Treatment"));


    }

    public void testValidateForWrongStudySegmentAttribute() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        ((Epoch) add1.getChild()).getStudySegments().get(0).setName("wrong name");
        String message = amendmentSerializer.validate(amendment, eAmendment);


        assertTrue(message.contains("name is different for StudySegment. expected:wrong name , found (in imported document) :A"));


    }


    public void testValidateForWrongNumberOfStudySegments() throws IOException, SAXException {

        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        List<StudySegment> studySegments = ((Epoch) add1.getChild()).getStudySegments();

        studySegments.clear();
        studySegments.add(studySegment2);
        String message = amendmentSerializer.validate(amendment, eAmendment);

        assertTrue(message.contains("Epoch present in the system and in the imported document must have identical number of study segments"));


    }

    public void testValidateForWrongNumberOfPeriods() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().clear();


        String message = amendmentSerializer.validate(amendment, eAmendment);
        assertTrue(message.contains("StudySegment[id=null] present in the system and in the imported document must have identical number of periods."));

    }

    public void testValidateForDifferentPeriodAttribute() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().first().setName("wrong name");


        String message = amendmentSerializer.validate(amendment, eAmendment);
        assertTrue(message.contains("Study segments must be identical and they must appear in the same order as they are in system."));

    }

    public void testValidateForWrongNumberOfPlannedActivities() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        List<PlannedActivity> plannedActivities = ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().first().getPlannedActivities();
        plannedActivities.clear();
        plannedActivities.add(plannedActivity1);

        String message = amendmentSerializer.validate(amendment, eAmendment);

        assertTrue(message.contains("Period[id=null] present in the system and in the imported document must have identical number of planned activities"));


    }

    //
    public void testValidateForWrongPlannedActivityContent() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        eAmendment = amendmentSerializer.createElement(amendment);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setDay(9);

        String message = amendmentSerializer.validate(amendment, eAmendment);

        assertTrue(message.contains("days  are different for PlannedActivity"));


    }


    public void testValidteElement() {
        assertFalse(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));
        assertEquals(String.format("\n released amendment present in the system does have  any delta matching with provied grid id %s and node id  %s of delta.\n",
                epochDelta.getGridId(), epoch1.getGridId()), epochDeltaXmlSerializer.validate(amendment, eDelta).toString());

        amendment.addDelta(epochDelta);
        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));

    }


    public void testValidateElementForDifferentNumberOfChanges() {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        assertEquals(String.format("Imported document has different number of Changes for  delta (id :%s).  Please make sure changes are identical and they are in same order." , epochDelta.getGridId())
                , epochDeltaXmlSerializer.validate(amendment, eDelta).toString());


    }

    public void testValidateForInValidAddChangeContent() throws IOException, SAXException {
        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setActivity(activity3);

        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("activity references are different for PlannedActivity"));


    }

    public void testValidateForInValidPropertyChangeAttributes() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        add1.setIndex(5);


        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("index is different. expected:5 , found (in imported document) :0"));


    }

    public void testValidateForIdenticalDelta() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));


    }
}