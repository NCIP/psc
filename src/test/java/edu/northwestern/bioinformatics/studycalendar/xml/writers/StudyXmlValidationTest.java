package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyImportException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Saurabh Agrawal
 */
public class StudyXmlValidationTest extends AbstractXmlValidationTest {
    private Element eStudy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        eStudy = studyXmlserializer.createElement(study);
    }



    public void testValidateIfStudyHasMoreNumberOfAmendments() throws IOException, SAXException {

        study.setAmendment(firstAmendment);
        expectResolveStudy(study);


        replayMocks();
        try {
            studyXmlserializer.validate(eStudy);
            fail();
        } catch (StudyImportException e) {
            String errorMessage = String.format("Imported document must have all released amendment presents in system. Study present in system has %s number of released amendments",
                    1);

            assertEquals(errorMessage, e.getMessage());
        }

        verifyMocks();

    }

    public void testValidateForNonExistingAmendments() throws IOException, SAXException {

        study.setAmendment(firstAmendment);

        eStudy = studyXmlserializer.createElement(study);
        expectResolveStudy(study);

        study.setAmendment(amendment);

        replayMocks();
        try {
            studyXmlserializer.validate(eStudy);
            fail();
        } catch (StudyImportException e) {
            assertTrue(e.getMessage().contains("A released amendment 08/16/2008 ([Second Expected]) present in the system is not present in the imported document"));
        }

        verifyMocks();

    }


    public void testValidateForWrongOrderOfAmendments() throws IOException, SAXException {

        study.setAmendment(firstAmendment);
        study.pushAmendment(amendment);
        expectResolveStudy(study);


        replayMocks();

        eStudy = studyXmlserializer.createElement(study);
        assertTrue(studyXmlserializer.validate(eStudy));
        verifyMocks();

    }

    private void expectResolveStudy(Study study) {
        expect(studyService.getStudyByAssignedIdentifier(study.getAssignedIdentifier())).andReturn(study);

    }

    public void testValidateIfDevelopmentAmendmentMatchesWithReleasedAmendment() throws IOException, SAXException {

        study.setDevelopmentAmendment(amendment);
        eStudy = studyXmlserializer.createElement(study);
        study.setAmendment(amendment);
        expectResolveStudy(study);

        replayMocks();
        try {
            studyXmlserializer.validate(eStudy);
            fail();
        } catch (StudyImportException e) {
            assertTrue(e.getMessage().contains("Imported document must not have any development amendment which matches with any relased amendment present in system"));
        }

        verifyMocks();

    }

    public void testReadValidXml() throws IOException, SAXException {
        amendment.getDeltas().add(plannedCalendarDelta);
        amendment.getDeltas().add(periodDelta);
        amendment.getDeltas().add(studySegmentDelta);
        amendment.getDeltas().add(epochDelta);

        study.setAmendment(amendment);

        firstAmendment.getDeltas().add(plannedCalendarDelta);
        study.pushAmendment(firstAmendment);
        eStudy = studyXmlserializer.createElement(study);
        expectResolveStudy(study);
        replayMocks();

        assertTrue(studyXmlserializer.validate(eStudy));

        verifyMocks();
    }


}