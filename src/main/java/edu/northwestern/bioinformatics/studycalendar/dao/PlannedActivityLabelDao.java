package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

public class PlannedActivityLabelDao extends StudyCalendarMutableDomainObjectDao<PlannedActivityLabel> {
    @Override
    public Class<PlannedActivityLabel> domainClass() {
        return PlannedActivityLabel.class;
    }
}
