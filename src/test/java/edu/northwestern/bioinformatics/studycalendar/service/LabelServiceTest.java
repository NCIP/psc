package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendments;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAddChange;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPeriod;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Sep 16, 2008
 * Time: 2:25:56 PM
 * To change this template use File | Settings | File Templates.
 */


//
//private LabelDao labelDao;
//private PlannedActivityDao plannedActivityDao;
//private PlannedActivityLabelDao plannedActivityLabelDao;
//protected final Logger log = LoggerFactory.getLogger(getClass());
//
//
//public boolean deleteLabel(Label label, PlannedActivity plannedActivity) {
//    List<PlannedActivityLabel> plannedActivityLabels = plannedActivityLabelDao.getPALabelByPlannedActivityIdAndLabelId(plannedActivity.getId(), label.getId());
//    if (plannedActivityLabels != null) {
//        for (PlannedActivityLabel palabel : plannedActivityLabels) {
//            plannedActivityLabelDao.delete(palabel);
//        }
//    }
//    labelDao.delete(label);
//    return true;
//}
//
//public Label getOrCreateLabel(String labelName) {
//    Label label = labelDao.getByName(labelName);
//    if (label == null) {
//        label = new Label();
//        label.setName(labelName);
//        try {
//            labelDao.save(label);
//        } catch(DataIntegrityViolationException ex) {
//            log.debug("Throwing exception due to constraint violation. Label with this name was added by other activity" + ex.getMessage());
//            label = labelDao.getByName(labelName);
//        }
//    }
//    return label;
//}
//
//
//public PlannedActivityLabel getOrCreatePlannedActivityLabel(Label label, PlannedActivity plannedActivity) {
//    PlannedActivityLabel plannedActivityLabel = plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity.getId(), label.getId(), plannedActivity.getPeriod().getRepetitions());
//    if (plannedActivityLabel == null) {
//        plannedActivityLabel = new PlannedActivityLabel();
//        plannedActivityLabel.setPlannedActivity(plannedActivity);
//        plannedActivityLabel.setLabel(label);
//        plannedActivityLabel.setRepetitionNumber(plannedActivity.getPeriod().getRepetitions());
//        try {
//            plannedActivityLabelDao.save(plannedActivityLabel);
//        } catch (DataIntegrityViolationException ex) {
//            plannedActivityLabel = plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity.getId(), label.getId(), plannedActivity.getPeriod().getRepetitions());
//        }
//    }
//    return plannedActivityLabel;
//}
//
//
////need to see if the space-delimiters exist in labelName
//public String[] getLabelsFromStringParameter (String labelName) {
//    return labelName.split(" ");
//}
//
//
//@Required
//public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
//    this.plannedActivityDao = plannedActivityDao;
//}
//
//@Required
//public void setLabelDao(LabelDao labelDao) {
//    this.labelDao = labelDao;
//}
//
//@Required
//public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
//    this.plannedActivityLabelDao = plannedActivityLabelDao;
//}





public class LabelServiceTest extends StudyCalendarTestCase {

    private LabelDao labelDao;
    private PlannedActivityDao plannedActivityDao;
    private PlannedActivityLabelDao plannedActivityLabelDao;
    private LabelService service;


    private Study study;
    private StudySubjectAssignment ladyPatient;
    private PlannedActivity plannedActivity1, plannedActivity2;
    private Label label1, label2;
    private PlannedActivityLabel paLabel1, paLabel2, paLabel3;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        labelDao = registerDaoMockFor(LabelDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        plannedActivityLabelDao = registerDaoMockFor(PlannedActivityLabelDao.class);


        service = new LabelService();
        service.setLabelDao(labelDao);
        service.setPlannedActivityDao(plannedActivityDao);
        service.setPlannedActivityLabelDao(plannedActivityLabelDao);

        Period period = createPeriod("dc", 1, 14, 1);
        plannedActivity1 = createPlannedActivity("Bone Test", 4);

        plannedActivity2 = createPlannedActivity("BCT", 2);
        period.addPlannedActivity(plannedActivity1);
        period.addPlannedActivity(plannedActivity2);

        label1 = Fixtures.createLabel("Label1");
        label1.setId(13);

        label2 = Fixtures.createLabel("Label2");
        label2.setId(14);

        paLabel1 = Fixtures.createPlannedActivityLabel(plannedActivity1, label1, 2);
        paLabel2 = Fixtures.createPlannedActivityLabel(plannedActivity2, label2, 3);
        paLabel3 = Fixtures.createPlannedActivityLabel(plannedActivity1, label1, 4);

    }

    public void testGetLabel() throws Exception {
        expect(labelDao.getByName("Label1")).andReturn(label1).anyTimes();
        replayMocks();
        Label label = service.getOrCreateLabel(label1.getName());
        verifyMocks();

        assertNotNull("Label is not null ", label);
        assertEquals("Label is not returned", label1.getId(), label.getId());
        assertEquals("Label is not same", label1, label);
    }


    public void testCreateLabel() throws Exception {
        expect(labelDao.getByName("Label3")).andReturn(null).anyTimes();
        Label label3 = new Label();
        label3.setName("Label3");
        labelDao.save(label3);

        replayMocks();
        Label label = service.getOrCreateLabel("Label3");
        verifyMocks();

        assertNotNull("Label is not null ", label);
        assertEquals("Label name is not correct ", "Label3", label.getName());
    }


    public void testGetPlannedActivityLabel() throws Exception {
        expect(plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity1.getId(),
                label1.getId(), plannedActivity1.getPeriod().getRepetitions())).andReturn(paLabel1).anyTimes();
        replayMocks();
        PlannedActivityLabel palabel = service.getOrCreatePlannedActivityLabel(label1, plannedActivity1);
        verifyMocks();
        assertNotNull("Label is not null ", palabel);
        assertEquals("Label is not returned", label1, palabel.getLabel());
        assertEquals("Label is not same", plannedActivity1, palabel.getPlannedActivity());
    }


    public void testCreatePlannedActivityLabel() throws Exception {
        expect(plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity2.getId(),
                label1.getId(), plannedActivity2.getPeriod().getRepetitions())).andReturn(null).anyTimes();
        PlannedActivityLabel plannedActivityLabel = new PlannedActivityLabel();
        plannedActivityLabel.setPlannedActivity(plannedActivity2);
        plannedActivityLabel.setLabel(label1);
        plannedActivityLabel.setRepetitionNumber(plannedActivity2.getPeriod().getRepetitions());
        plannedActivityLabelDao.save(plannedActivityLabelEq(plannedActivityLabel));

        replayMocks();
        PlannedActivityLabel newPALabel = service.getOrCreatePlannedActivityLabel(plannedActivityLabel.getLabel(), plannedActivityLabel.getPlannedActivity());
        verifyMocks();

        assertNotNull("Expected PALabel is not null ", newPALabel);
        assertEquals("PlannedActivity is not correct ", plannedActivity2, newPALabel.getPlannedActivity());
        assertEquals("Label is not correct ", label1, newPALabel.getLabel());
    }


    public void testDeletePlannedActivityLabel() throws Exception {
        List<PlannedActivityLabel> labels = new ArrayList<PlannedActivityLabel>();
        labels.add(paLabel1);
        labels.add(paLabel3);
        expect(plannedActivityLabelDao.getPALabelByPlannedActivityIdAndLabelId(plannedActivity1.getId(), label1.getId())).andReturn(labels).anyTimes();
        for (PlannedActivityLabel label:labels) {
            plannedActivityLabelDao.delete(label);
        }

        labelDao.delete(label1);

        replayMocks();
        Boolean result = service.deleteLabel(label1, plannedActivity1);
        verifyMocks();

        assertEquals("Expected result is false ",true, (boolean) result);
    }
     ////// CUSTOM MATCHERS

    private static PlannedActivityLabel plannedActivityLabelEq(PlannedActivityLabel expectedPALabel) {
        EasyMock.reportMatcher(new PlannedActivityLabelMatcher(expectedPALabel));
        return null;
    }

    private static class PlannedActivityLabelMatcher implements IArgumentMatcher {
        private PlannedActivityLabel expectedPALabel;

        public PlannedActivityLabelMatcher(PlannedActivityLabel expectedPALabel) {
            this.expectedPALabel = expectedPALabel;
        }

        public boolean matches(Object object) {
            PlannedActivityLabel actual = (PlannedActivityLabel) object;

            if (expectedPALabel.getLabel().equals(actual.getLabel())) {
                if (expectedPALabel.getPlannedActivity().equals(actual.getPlannedActivity())) {
                    if(expectedPALabel.getRepetitionNumber().equals(actual.getRepetitionNumber()))
                    return true;
                }
            }

            return false;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("PlannedActivityLabel=").append(expectedPALabel);
        }
    }

}
