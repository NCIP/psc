/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import gov.nih.nci.cagrid.common.Utils;
import junit.framework.TestCase;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.utils.StringBufferReader;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;

import javax.xml.XMLConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: saurabhagrawal
 * Date: Jan 15, 2008
 * Time: 12:16:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSCStudyServiceTest extends TestCase {

    private Study study;

    private edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy;
    private String regFile;

    private StudyXMLWriter studyXMLWriter;
    private final String ASSIGNED_IDENTIFIER = "assigned_identifier";
    private Log logger = LogFactory.getLog(getClass());

    protected void setUp() throws Exception {
        study = new Study();

        study = createStudy("Study A");
        study.setAmendment(createAmendment());

        PlannedCalendar plannedCalendar = new PlannedCalendar();
        plannedCalendar.setId(3);
        plannedCalendar.setGridId("planned_calendar");
        plannedCalendar.setStudy(study);
        study.setPlannedCalendar(plannedCalendar);


        gridStudy = new edu.northwestern.bioinformatics.studycalendar.grid.Study();

        regFile = System.getProperty("psc.test.sampleStudyFile",
                "grid/study-consumer/test/resources/SampleStudyMessage.xml");
        studyXMLWriter = new StudyXMLWriter();

    }

    public void testCopy() throws Exception {
//        InputStream config = Thread.currentThread().getContextClassLoader().
//                          getResourceAsStream("edu/northwestern/bioinformatics/studycalendar/grid/client/client-config.wsdd");

                    //example of this study xml
//            <?xml version="1.0" encoding="UTF-8"?>
//            <study xmlns="http://bioinformatics.northwestern.edu/ns/psc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//          assigned-identifier="cc" id="96507ff5-06e7-4f45-bfc5-179b888e69f6"
//              xsi:schemaLocation="http://bioinformatics.northwestern.edu/ns/psc
// http://bioinformatics.northwestern.edu/ns/psc/psc.xsd">
//            <planned-calendar id="9a2ec400-ef49-4d7d-9917-5766193c8181"/>
//            </study>
//        String studyDocumentXml= studyXmlSerializer.createDocumentString(gridStudy);
//        logger.debug("study doc xml"+studyDocumentXml);

        String studyXML = studyXMLWriter.createStudyXML(study);
        validate(studyXML, true);
        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = populateGridStudy(studyXML);
        assertNotNull(gridStudy);

        //now serialize back this grid study to study xml
        StringWriter studyXml = new StringWriter();

        Utils.serializeObject(gridStudy,
                new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc","study",XMLConstants.W3C_XML_SCHEMA_NS_URI)
                , studyXml);

        //logger.info("study xml:" + studyXml.toString());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyXml.getBuffer().toString().getBytes());

        StudyXMLReader studyXMLReader=new StudyXMLReader();
       
        studyXMLReader.readAndSave(byteArrayInputStream);


    }

    private edu.northwestern.bioinformatics.studycalendar.grid.Study populateGridStudy(String studyXML) throws Exception {
        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().
                    getResourceAsStream("edu/northwestern/bioinformatics/studycalendar/grid/client/client-config.wsdd");
            Reader reader = new StringBufferReader(new StringBuffer(studyXML));
            gridStudy = (edu.northwestern.bioinformatics.studycalendar.grid.Study) Utils.deserializeObject(reader,
                    edu.northwestern.bioinformatics.studycalendar.grid.Study.class, config);
        }
        catch (Exception exception) {
            logger.error("errror while exporting study. genreated study xml:" + studyXML + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }
        return gridStudy;
    }


    public Study createStudy(String name) throws Exception {
        Study newStudy = new Study();
        newStudy.setGridId("grid study");
        newStudy.setId(1);
        newStudy.setAssignedIdentifier(ASSIGNED_IDENTIFIER);
        return newStudy;
    }

    public Amendment createAmendment() throws Exception {
        Amendment newAmendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        newAmendment.setDate(new Date());
        newAmendment.setGridId("grid amendment");
        newAmendment.setId(2);
        return newAmendment;
    }

    private void validate(String studyXml, Boolean validStdyXmlString) {
        assertNotNull(studyXml);
        byte[] byteOutput = studyXml.getBytes();
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        if (validStdyXmlString) {
            assertFalse("Template xml should be error free", errors.hasErrors());
        } else {
            assertTrue("Template xml has errors", errors.hasErrors());

        }
    }

}
