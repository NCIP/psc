package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jalpa Patel
 * Date: Aug 26, 2008
*/
public class CycleCommand {
    private static final Logger log = LoggerFactory.getLogger(CycleCommand.class.getName());

    private StudySegmentDao studySegmentDao;

    private Integer cycleLength;
    private StudySegment studySegment;

    public CycleCommand(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    public void apply() {
        studySegment.setCycleLength(cycleLength);
        studySegmentDao.save(studySegment);
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
