package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import org.dom4j.Element;

public class EpochXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String EPOCH = "epoch";

    private EpochDao epochDao;


    protected PlanTreeNode<?> nodeInstance() {
        return new Epoch();
    }

    protected String elementName() {
        return EPOCH;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return epochDao.getByGridId(id);
    }

    protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
        StudySegmentXmlSerializer serializer = (StudySegmentXmlSerializer) getBeanFactory().getBean("studySegmentXmlSerializer");
        serializer.setStudy(study);
        return serializer;
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Epoch) node).setName(element.attributeValue(NAME));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((Epoch) node).getName());
    }

    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) {
        if (getChildSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}