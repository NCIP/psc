package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;

import java.beans.PropertyEditorSupport;

/**
 * @author Rhett Sutphin
 */
public class PscUserEditor extends PropertyEditorSupport {
    private final PscUserService pscUserService;

    public PscUserEditor(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        PscUser found = pscUserService.getAuthorizableUser(text);
        if (found == null) {
            throw new IllegalArgumentException("No user named \"" + text + '"');
        } else {
            setValue(found);
        }
    }

    @Override
    public String getAsText() {
        return getValue() == null ? null : ((PscUser) getValue()).getUsername();
    }
}
