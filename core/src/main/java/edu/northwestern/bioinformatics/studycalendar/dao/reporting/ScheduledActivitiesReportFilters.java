package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.Collection;
import java.util.Date;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportFilters extends ReportFilters {
    private SubstringFilterLimit studyAssignedIdentifier = new SubstringFilterLimit("studyAssignedIdentifier");
    private SubstringFilterLimit siteName = new SubstringFilterLimit("siteName");
    private ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>> currentStateMode =
        new ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>>("currentStateMode");
    private RangeFilterLimit<Date> actualActivityDate = new RangeFilterLimit<Date>("actualActivityDate");

    private DomainObjectFilterLimit<ActivityType> activityType =  new DomainObjectFilterLimit<ActivityType>("activityType");
    private ResponsibleUserFilterLimit responsibleUser = new ResponsibleUserFilterLimit();
    private StringFilter label = new StringFilter("label");
    private StringFilter personId = new StringFilter("personId");
    private InListFilterLimit<Integer> authorizedStudySiteIds =
        new InListFilterLimit<Integer>("authorizedStudySiteIds");

    @Override
    protected String getHibernateFilterPrefix() {
        return "filter_";
    }

    public String getStudyAssignedIdentifier() {
        return studyAssignedIdentifier.getValue();
    }

    public void setStudyAssignedIdentifier(String value) {
        studyAssignedIdentifier.setValue(value);
    }

    public void setCurrentStateMode(ScheduledActivityMode<?> mode) {
        currentStateMode.setValue(mode);
    }

    public ScheduledActivityMode<?> getCurrentStateMode() {
        return currentStateMode.getValue();
    }

    public String getSiteName() {
        return siteName.getValue();
    }

    public void setSiteName(String value) {
        siteName.setValue(value);
    }

    public MutableRange<Date> getActualActivityDate() {
        return actualActivityDate.getValue();
    }

    public void setActualActivityDate(MutableRange<Date> range) {
        this.actualActivityDate.setValue(range);
    }

    public void setActivityType(ActivityType type) {
        activityType.setValue(type);
    }

    public ActivityType getActivityType() {
        return activityType.getValue();
    }

    public User getResponsibleUser() {
        return responsibleUser.getValue();
    }

    public void setResponsibleUser(User responsibleUser) {
        this.responsibleUser.setValue(responsibleUser);
    }

    public String getLabel() {
        return label.getValue();
    }

    public void setLabel(String value) {
        label.setValue(value);
    }

    public String getPersonId() {
        return personId.getValue();
    }

    public void setPersonId(String value) {
        personId.setValue(value);
    }

    public Collection<Integer> getAuthorizedStudySiteIds() {
        return authorizedStudySiteIds.getValue();
    }

    public void setAuthorizedStudySiteIds(Collection<Integer> ids) {
        this.authorizedStudySiteIds.setValue(ids);
    }

    //////

    private class ResponsibleUserFilterLimit extends SingleFilterFilterLimit<Long, User> {
        private ResponsibleUserFilterLimit() {
            super("responsibleUser");
        }

        @Override
        protected Long getValueForFilter() {
            return getValue().getUserId();
        }
    }
}
