package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

/**
 * @author Rhett Sutphin
*/
public class TestingTemplateService extends TemplateService {
    @Override
    public <P extends PlanTreeNode<?>> P findParent(PlanTreeNode<P> node) {
        return node.getParent();
    }
}
