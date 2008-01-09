package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;
import junit.framework.AssertionFailedError;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: saurabhagrawal
 * Date: Jan 9, 2008
 * Time: 12:34:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSCStudyImportExportIntegrationTest extends AbstractTransactionalSpringContextTests {

    private PSCStudyImportExport studyImportExport;
    private String coordinatingCenterIdentifier;


    private StudyDao studyDao;

    private int id = 1;

    Study study;
    private Amendment amendment;

    private AmendmentDao amendmentDao;

    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-study-import-export.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        coordinatingCenterIdentifier = "cc";

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));


    }

    protected void onTearDownAfterTransaction() throws Exception {
        super.onTearDownAfterTransaction();
        DataAuditInfo.setLocal(null);
    }

    public void testStudyExportWhenStudyDoesNotExistsForIdentifier() throws Exception {
        try {
            studyImportExport.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
            fail("no study exists with given identifier");
        } catch (RemoteException e) {
            //expecting this exception
        }

    }

    public void testStudyExportForEmptyStudy() throws Exception {
        createStudy("Study A");


        String studyXml = studyImportExport.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        try {
            validate(studyXml.getBytes());
            fail("Template xml should be error free");
        } catch (AssertionFailedError e) {
            //expecting this
        }


        amendment = createAmendment();
        amendmentDao.save(amendment);

        study = createStudy("Study A");
        study.setAmendment(amendment);

        PlannedCalendar plannedCalendar = new PlannedCalendar();
        plannedCalendar.setStudy(study);
        study.setPlannedCalendar(plannedCalendar);
        studyDao.save(study);

        studyXml = studyImportExport.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        validate(studyXml.getBytes());


    }


    public void testStudyExport() throws Exception {

        amendment = createAmendment();
        amendmentDao.save(amendment);

        study = createStudy("Study A");
        study.setAmendment(amendment);

        PlannedCalendar plannedCalendar = new PlannedCalendar();
        plannedCalendar.setStudy(study);
        study.setPlannedCalendar(plannedCalendar);
        studyDao.save(study);

        String studyXml = studyImportExport.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        validate(studyXml.getBytes());


    }

    public Amendment createAmendment() throws Exception {
        Amendment newAmendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        newAmendment.setDate(new Date());
        //setGridId(newAmendment);
        return newAmendment;
    }


    private void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }


    private Study createStudy(String longTitle) {
        Study pscStudy = new Study();
        pscStudy.setLongTitle("long title");
        pscStudy.setAssignedIdentifier(coordinatingCenterIdentifier);
        studyDao.save(pscStudy);
        assertNotNull(pscStudy.getId());
        return pscStudy;
    }

    @Required
    public void setStudyImportExport(PSCStudyImportExport studyImportExport) {
        this.studyImportExport = studyImportExport;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
