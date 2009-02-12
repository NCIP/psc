package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;

/**
 * @author Rhett Sutphin
*/
public class TestingTemplateService extends TemplateService {
    @Override
    public <P extends Parent> P findParent(Child<P> node) {
        return node.getParent();
    }
}
