package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;

/**
 * @author Rhett Sutphin
*/
public class AmendmentView {
    private Amendment amendment;
    private RevisionChanges revisionChanges;

    public AmendmentView(Study study, Amendment amendment, DaoFinder daoFinder) {
        this.amendment = amendment;
        this.revisionChanges = new RevisionChanges(daoFinder, amendment, study);
    }

    public Amendment getAmendment() {
        return amendment;
    }

    public RevisionChanges getChanges() {
        return revisionChanges;
    }
}
