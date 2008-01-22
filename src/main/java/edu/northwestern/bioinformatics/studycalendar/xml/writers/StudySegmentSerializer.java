package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.dom4j.Element;

public class StudySegmentSerializer extends PlanTreeNodeXmlSerializer{
  
    public static final String STUDY_SEGMENT = "study-segment";

    private StudySegmentDao studySegmentDao;
    private PlanTreeNodeXmlSerializer periodXmlSerializer;

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
        return periodXmlSerializer;
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

    public void setPeriodXmlSerializer(PlanTreeNodeXmlSerializer periodXmlSerializer) {
        this.periodXmlSerializer = periodXmlSerializer;
    }
}
