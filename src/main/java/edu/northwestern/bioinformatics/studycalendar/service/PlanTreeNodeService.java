package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class PlanTreeNodeService {

    private DeltaService deltaService;

    @SuppressWarnings({"unchecked"})
    //public <T extends PlanTreeNode<?>>
    public <T extends PlanTreeInnerNode<?, ?, ?>> T copy(T source, final boolean isDevelopmentTemplateSelected) {

        T revisedSource = null;
        if (isDevelopmentTemplateSelected) {
            revisedSource = deltaService.revise(source);
        } else {
            revisedSource = source;
            //user has selected the releaesd template so dont revise the study
        }

        T copiedSource = (T) revisedSource.transientClone();

        copiedSource.setId(null);
        copiedSource.setGridId(null);
        copiedSource.setParent(null);

        Collection<?> childerns = copiedSource.getChildren();

        for (Object child : childerns) {
            if (child instanceof PlanTreeNode) {

                ((PlanTreeNode) child).setId(null);
                ((PlanTreeNode) child).setGridId(null);
            }
        }

        return copiedSource;

    }

    @Required
    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
