package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class RenameControllerTest extends ControllerTestCase {
    private RenameController controller;

    private StudyDao studyDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        armDao = registerDaoMockFor(ArmDao.class);

        controller = new RenameController();
        controller.setStudyDao(studyDao);
        controller.setEpochDao(epochDao);
        controller.setArmDao(armDao);

        request.addParameter("name", "new name");
        study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().addEpoch(createEpoch("E", "A", "B"));
    }

    public void testRenameStudy() throws Exception {
        request.addParameter("study", "14");
        expect(studyDao.getById(14)).andReturn(setId(14, study));
        studyDao.save((Study) notNull());

        doHandle();
    }

    public void testRenameEpoch() throws Exception {
        request.addParameter("epoch", "22");
        expect(epochDao.getById(22)).andReturn(setId(22, study.getPlannedCalendar().getEpochs().get(0)));
        studyDao.save((Study) notNull());

        doHandle();
    }

    public void testRenameArm() throws Exception {
        request.addParameter("arm", "45");
        expect(armDao.getById(45)).andReturn(setId(45, study.getPlannedCalendar().getEpochs().get(0).getArms().get(1)));
        studyDao.save((Study) notNull());

        doHandle();
    }

    private void doHandle() throws Exception {
        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

}
