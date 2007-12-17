package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

import static java.lang.String.valueOf;

import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Date;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;

public class StudyXmlWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private char id = 'a';
    
    private Study study;
    private StudyXmlWriter witer;

    protected void setUp() throws Exception {
        super.setUp();

        witer = new StudyXmlWriter();

        study = Fixtures.createBasicTemplate();

        Add add = createAdd(createNamedInstance("Segment 0", StudySegment.class), 0);
        Delta<Epoch> delta = createDeltaFor(study.getPlannedCalendar().getEpochs().get(0), add);
        study.setDevelopmentAmendment(createAmendment(study, delta));

        // TODO: Clean this up
        setGridIds( study,
                    study.getPlannedCalendar(),
                    study.getAmendmentsList().get(0),
                    study.getAmendmentsList().get(0).getDeltas().get(0),
                    study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(0),
                    study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(1),
                    study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(2),
                    ((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(0)).getChild(),
                    ((Epoch)((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(0)).getChild()).getStudySegments().get(0),
                    ((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(1)).getChild(),
                    ((Epoch)((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(0),
                    ((Epoch)((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(1),
                    ((Epoch)((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(2),
                     ((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(2)).getChild(),
                    ((Epoch)((ChildrenChange) study.getAmendmentsList().get(0).getDeltas().get(0).getChanges().get(2)).getChild()).getStudySegments().get(0)
        );
    }

    public void testContainsRoot() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.ROOT);
    }

    public void testContainsPlannedCalendar() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.PLANNDED_CALENDAR);
    }

    public void testContainsAmendment() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.AMENDMENT);
    }

    public void testContainsDelta() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.PLANNED_CALENDAR_DELTA);
    }

    public void testContainsAddChange() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.ADD);
    }

    public void testContainsEpoch() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.EPOCH);
    }

    public void testContainsStudySegment() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.STUDY_SEGMENT);
    }

    public void testContainsEpochDelta() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.EPOCH_DELTA);
    }

    /* Test Helpers */

    public String createAndValidateXml(Study study) throws Exception{
        String s = witer.createStudyXml(study);
        log.debug("XML: {}", s);
        
        validate(s.getBytes());

        return s;
    }

    private void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, StringUtils.EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }


    private void assertContainsTag(String output, String element) {
        assertContains(output, toTag(element));
    }

    private String toTag(String element) {
        return "<" + element;
    }

    private void setGridIds(GridIdentifiable... objects) throws Exception {
        for(GridIdentifiable object : objects) {
            object.setGridId(valueOf(nextGridId()));
        }
    }

    private <T extends GridIdentifiable> T setGridId(T object) throws Exception{
        setGridIds(object);
        return object;
    }

    private String nextGridId() {
        return valueOf(id++);
    }

    public Amendment createAmendment(Study study, Delta delta) throws Exception {
        Amendment amendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        amendment.setDate(new Date());
        amendment.addDelta(delta);
        setGridId(amendment);
        return amendment;
    }
    
    private  <T extends Named & GridIdentifiable> T createNamedInstance(String name, Class<T> clazz) throws Exception {
        return setGridId(Fixtures.createNamedInstance(name, clazz));
    }


    private <T extends PlanTreeNode<? extends GridIdentifiable>> Delta<T> createDeltaFor(T node, Change... changes) throws Exception {
        return setGridId(Delta.createDeltaFor(node, changes));
    }

    private Add createAdd(PlanTreeNode<?> child, int index) throws Exception {
        return setGridId(Add.create(child, index));
    }
}
