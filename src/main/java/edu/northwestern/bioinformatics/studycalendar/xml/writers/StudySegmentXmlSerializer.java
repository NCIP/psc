package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;

public class StudySegmentXmlSerializer extends PlanTreeNodeXmlSerializer{
  
    public static final String STUDY_SEGMENT = "study-segment";

    private StudySegmentDao studySegmentDao;

    public StudySegmentXmlSerializer(Study study) {
        super(study);
    }

    protected Class<?> nodeClass() {
        return StudySegment.class;
    }

    protected String elementName() {
        return STUDY_SEGMENT;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return studySegmentDao.getByGridId(id);
    }

    protected PlanTreeNodeXmlSerializer getChildSerializer() {
        return new PeriodXmlSerializer(study);
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((StudySegment) node).setName(element.attributeValue(NAME));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((StudySegment) node).getName());
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
