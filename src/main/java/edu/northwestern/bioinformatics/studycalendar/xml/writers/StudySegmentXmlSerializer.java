package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.dom4j.Element;

import java.util.List;
import java.util.SortedSet;

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

    protected PeriodXmlSerializer getPeriodXmlSerializer() {
        return (PeriodXmlSerializer) getChildSerializer();
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

    @Override
    public String validateElement(PlanTreeNode<?> planTreeNode, Element element) {
        StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));
        StudySegment studySegment = (StudySegment) planTreeNode;
        if (!studySegment.getName().equals(element.attributeValue(NAME))) {
            errorMessageStringBuffer.append(String.format("name is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", studySegment.getName(), element.attributeValue(NAME)));

        }

        List childElements = element.elements();
        SortedSet<Period> periods = studySegment.getPeriods();

        if ((childElements == null && periods != null)
                || (childElements != null && periods == null)
                || (periods.size() != childElements.size())) {
            errorMessageStringBuffer.append(String.format("%s present in the system and in the imported document must have identical number of periods.\n",
                    studySegment.toString()));


        } else {
            for (int i = 0; i < childElements.size(); i++) {
                Element childElement = (Element) childElements.get(i);

                Period period = getPeriodXmlSerializer().getPeriodWithMatchingGridId(periods, childElement);
                if (period == null) {
                    errorMessageStringBuffer.append(String.format("A period with grid id %s present in imported document is not present in system. \n",
                            childElement.attributeValue(ID)));
                    break;
                }
                errorMessageStringBuffer.append(getPeriodXmlSerializer().validateElement(period, childElement));

            }
        }

        return errorMessageStringBuffer.toString();
    }


}
