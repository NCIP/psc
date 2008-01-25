package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;

public class EpochXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String EPOCH = "epoch";

    private EpochDao epochDao;

    public EpochXmlSerializer(Study study) {
        super(study);
    }

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
        return new StudySegmentXmlSerializer(getStudy());
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Epoch) node).setName(element.attributeValue(NAME));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((Epoch) node).getName());
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}