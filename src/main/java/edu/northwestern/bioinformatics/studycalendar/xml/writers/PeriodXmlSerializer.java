package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;

public class PeriodXmlSerializer extends PlanTreeNodeXmlSerializer {
    public static final String PERIOD = "period";

    private PeriodDao periodDao;

    public PeriodXmlSerializer(Study study) {
        super(study);
    }

    protected Class<?> nodeClass() {
        return Period.class;
    }

    protected String elementName() {
        return PERIOD;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return periodDao.getByGridId(id);
    }

    protected PlanTreeNodeXmlSerializer getChildSerializer() {
        return new PlannedActivityXmlSerializer(study);
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Period) node).setName(element.attributeValue(NAME));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((Period) node).getName());
    }

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
