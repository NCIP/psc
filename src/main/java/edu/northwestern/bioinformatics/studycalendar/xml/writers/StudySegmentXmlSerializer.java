package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.dom4j.Element;

public class StudySegmentXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
  
    public static final String STUDY_SEGMENT = "study-segment";

    private StudySegmentDao studySegmentDao;

    protected PlanTreeNode<?> nodeInstance() {
        return new StudySegment();
    }

    protected String elementName() {
        return STUDY_SEGMENT;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return studySegmentDao.getByGridId(id);
    }

    protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
        PeriodXmlSerializer serializer = (PeriodXmlSerializer) getBeanFactory().getBean("periodXmlSerializer");
        serializer.setStudy(study);
        return serializer;
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((StudySegment) node).setName(element.attributeValue(NAME));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((StudySegment) node).getName());
    }

    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) {
        if (getChildSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
