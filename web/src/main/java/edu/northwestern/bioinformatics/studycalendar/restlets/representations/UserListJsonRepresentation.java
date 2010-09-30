package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.restlets.representations.JacksonTools.*;

/**
 * @author Rhett Sutphin
 */
public class UserListJsonRepresentation extends StreamingJsonRepresentation {
    private List<PscUser> users;
    private boolean includeRoles;
    private List<Site> visibleSites;
    private List<String> visibleManagedStudyIdentifiers;
    private List<String> visibleParticipatingStudyIdentifiers;
    private Integer limit, offset, total;

    /**
     * @param users The user objects to be rendered.
     *      This may be a sublist of all the users in the system.
     * @param brief Should the users be rendered using the brief representation?
     * @param total The total number of users matching the query
     *      (not the number being represented here)
     * @param offset The index of the first user we are rendering relative to all the users
     * @param limit The user-specified count to return. May be greater than users.size().
     *      Also may be null.
     */
    public UserListJsonRepresentation(
        List<PscUser> users, boolean brief, int total, Integer offset, Integer limit
    ) {
        this.users = users;
        this.total = total;
        this.offset = offset == null ? 0 : offset;
        this.limit = limit;
        this.visibleSites = Collections.emptyList();
        this.visibleManagedStudyIdentifiers = Collections.emptyList();
        this.visibleParticipatingStudyIdentifiers = Collections.emptyList();
        this.includeRoles = !brief;
    }

    @Override
    public void generate(JsonGenerator g) throws IOException, JsonGenerationException {
        g.writeStartObject();
            g.writeNumberField("total", total);
            g.writeNumberField("offset", offset);
            if (limit != null) {
                g.writeNumberField("limit", limit);
            }
            g.writeFieldName("users");
            writeUsersArray(g);
        g.writeEndObject();
    }

    private void writeUsersArray(JsonGenerator g) throws IOException {
        g.writeStartArray();
        for (PscUser user : getUsers()) {
            g.writeStartObject();
                g.writeStringField("username", user.getUsername());
                nullSafeWriteStringField(g, "first_name", user.getCsmUser().getFirstName());
                nullSafeWriteStringField(g, "last_name", user.getCsmUser().getLastName());
                g.writeStringField("display_name", user.getDisplayName());
                nullSafeWriteDateField(g, "end_date", user.getCsmUser().getEndDate());
                if (includeRoles) {
                    g.writeFieldName("roles");
                    writeRolesArray(g, user);
                }
            g.writeEndObject();
        }
        g.writeEndArray();
    }

    private void writeRolesArray(JsonGenerator g, PscUser user) throws IOException {
        g.writeStartArray();
        for (Map.Entry<SuiteRole, SuiteRoleMembership> entry : user.getMemberships().entrySet()) {
            SuiteRole role = entry.getKey();
            SuiteRoleMembership srm = entry.getValue();
            g.writeStartObject();
                g.writeStringField("key", role.getCsmName());
                g.writeStringField("display_name", role.getDisplayName());
                if (role.isSiteScoped()) {
                    if (srm.isAllSites()) {
                        g.writeBooleanField("all_sites", true);
                    } else {
                        g.writeFieldName("sites");
                        writeApplicableSitesArray(g, srm);
                    }
                }
                if (role.isStudyScoped()) {
                    if (srm.isAllStudies()) {
                        g.writeBooleanField("all_studies", true);
                    } else {
                        g.writeFieldName("studies");
                        writeApplicableStudiesArray(g, srm);
                    }
                }
            g.writeEndObject();
        }
        g.writeEndArray();
    }

    private void writeApplicableSitesArray(
        JsonGenerator g, SuiteRoleMembership srm
    ) throws IOException {
        g.writeStartArray();
        for (Site site : getVisibleSites()) {
            if (srm.getSiteIdentifiers().contains(site.getAssignedIdentifier())) {
                g.writeStartObject();
                    nullSafeWriteStringField(g, "identifier", site.getAssignedIdentifier());
                    nullSafeWriteStringField(g, "name", site.getName());
                g.writeEndObject();
            }
        }
        g.writeEndArray();
    }

    private void writeApplicableStudiesArray(
        JsonGenerator g, SuiteRoleMembership srm
    ) throws IOException {
        Set<String> visibleStudies = new LinkedHashSet<String>();
        PscRole role = PscRole.valueOf(srm.getRole());
        boolean isManagement = role != null &&
            role.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT);
        boolean isParticipation = role != null &&
            role.getUses().contains(PscRoleUse.SITE_PARTICIPATION);
        if (isManagement) {
            visibleStudies.addAll(getVisibleManagedStudyIdentifiers());
        }
        if (isParticipation || !isManagement) { // use participation for other roles
            visibleStudies.addAll(getVisibleParticipatingStudyIdentifiers());
        }
        g.writeStartArray();
        for (String visibleStudy : visibleStudies) {
            if (srm.getStudyIdentifiers().contains(visibleStudy)) {
                g.writeStartObject();
                    nullSafeWriteStringField(g, "identifier", visibleStudy);
                g.writeEndObject();
            }
        }
        g.writeEndArray();
    }

    ////// ACCESSORS

    public List<PscUser> getUsers() {
        return users;
    }

    public boolean getIncludeRoles() {
        return includeRoles;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getTotal() {
        return total;
    }

    ////// CONFIGURATION

    public List<Site> getVisibleSites() {
        return visibleSites;
    }

    public void setVisibleSites(List<Site> visibleSites) {
        this.visibleSites = visibleSites;
    }

    public List<String> getVisibleManagedStudyIdentifiers() {
        return visibleManagedStudyIdentifiers;
    }

    public void setVisibleManagedStudyIdentifiers(List<String> visibleManagedStudyIdentifiers) {
        this.visibleManagedStudyIdentifiers = visibleManagedStudyIdentifiers;
    }

    public List<String> getVisibleParticipatingStudyIdentifiers() {
        return visibleParticipatingStudyIdentifiers;
    }

    public void setVisibleParticipatingStudyIdentifiers(
        List<String> visibleParticipatingStudyIdentifiers
    ) {
        this.visibleParticipatingStudyIdentifiers = visibleParticipatingStudyIdentifiers;
    }
}
