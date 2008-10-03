package edu.northwestern.bioinformatics.studycalendar.web.template;


import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

/**
 * @author Jalpa Patel
 * Date: Aug 26, 2008
*/
public class CycleCommand {
    private static final Collection<String> PROPERTIES_TO_UPDATE
           = Arrays.asList("cycleLength");
    private TemplateService templateService;
    private Integer cycleLength;
    private StudySegment studySegment;
    private StudySegment oldStudySegment;
    private AmendmentService amendmentService;
    public CycleCommand(TemplateService templateService,AmendmentService amendmentService) {
        this.templateService = templateService;
        this.amendmentService = amendmentService;
    }

    public void apply() {
        List<Change> changes = new ArrayList<Change>();
        oldStudySegment = (StudySegment) studySegment.transientClone();
        studySegment.setCycleLength(cycleLength);
        updateCycleLengthWithChanges(changes);
        if (!changes.isEmpty()) {
            amendmentService.updateDevelopmentAmendment(studySegment,
                changes.toArray(new Change[changes.size()]));
        }
    }

    private void updateCycleLengthWithChanges(List<Change> target) {
        BeanWrapper originalWrapped = new BeanWrapperImpl(oldStudySegment);
        for (String prop : PROPERTIES_TO_UPDATE) {
            Object oldV = originalWrapped.getPropertyValue(prop);
            Object newV = studySegment.getCycleLength();
            if (!ComparisonTools.nullSafeEquals(oldV, newV)) {
                target.add(PropertyChange.create(prop, oldV, newV));
            }
        }
    }

    ////// BOUND PROPERTIES

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }

    public Integer getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(Integer cycleLength) {
        this.cycleLength = cycleLength;
    }
}
