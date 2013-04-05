/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Nataliya Shurupova
 */
@Transactional(propagation = Propagation.REQUIRED)
public class LabelService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private PlannedActivityLabelDao plannedActivityLabelDao;

    /**
     * Searches all the plannedActivityLabels in the system for those that match the given
     * criteria.  Returns a list of labels as a string containing just the
     * matching search.
     */
    public List<String> getFilteredLabels(String search) {
        List<PlannedActivityLabel> matches = plannedActivityLabelDao.getPALabelsSearchText(search);
        List<String> labels = getUniqueLabels(matches);
        return labels;
    }

    public List<String> getUniqueLabels(List<PlannedActivityLabel> paLabels) {
        List<String> labels= new ArrayList<String>();
        for (PlannedActivityLabel paLabel: paLabels) {
            labels.add(paLabel.getLabel());
        }

        Set<String> unique = new LinkedHashSet<String>(labels);
        return new ArrayList<String>(unique);
    }
    ////// CONFIGURATION

    @Required
    public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
        this.plannedActivityLabelDao = plannedActivityLabelDao;
    }    
}
