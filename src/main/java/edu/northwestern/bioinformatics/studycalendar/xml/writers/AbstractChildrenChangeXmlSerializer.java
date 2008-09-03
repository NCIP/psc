package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactoryAware;

public abstract class AbstractChildrenChangeXmlSerializer extends AbstractChangeXmlSerializer implements BeanFactoryAware {
    private static final String CHILD_ID = "child-id";
    protected DaoFinder daoFinder;
    protected Class<? extends PlanTreeNode> childClass;

    protected void addAdditionalAttributes(final Change change, Element element) {
        //element.addAttribute(CHILD_ID, ((ChildrenChange)change).getChild().getGridId());
        // Child is not stored on ChildChange, must retreive from object id
        PlanTreeNode<?> child = (PlanTreeNode<?>) getChild((ChildrenChange) change, childClass);
        element.addAttribute(CHILD_ID, child.getGridId());
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        String childId = element.attributeValue(CHILD_ID);
        Element child = getElementById(element, childId);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(child);
        PlanTreeNode<?> node = serializer.readElement(child);
        ((ChildrenChange)change).setChild(node);
    }

    // Methods to get child from child id
    protected DomainObject getChild(ChildrenChange change, Class<? extends PlanTreeNode> childClass) {
        if (change.getChild() != null) {
            return change.getChild();
        } else {
            DomainObjectDao<?> dao = getDaoFinder().findDao(childClass);
            return dao.getById(change.getChildId());
        }
    }

    public void setChildClass(Class childClass) {
        this.childClass = childClass;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public DaoFinder getDaoFinder() {
        return daoFinder;
    }
}
