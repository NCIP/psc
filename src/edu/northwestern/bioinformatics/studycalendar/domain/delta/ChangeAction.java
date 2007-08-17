package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.*;

/**
 * @author Rhett Sutphin
 */
public enum ChangeAction implements CodedEnum<String> {
    ADD,
    REMOVE,
    REORDER,
    CHANGE_PROPERTY("property")
    ;

    private String code;

    ChangeAction() {
        this(null);
    }

    ChangeAction(String code) {
        this.code = code;
        register(this);
    }

    public String getCode() {
        return code == null ? name().toLowerCase() : code;
    }

    public static ChangeAction getByCode(String code) {
        return getByClassAndCode(ChangeAction.class, code);
    }

    public String getDisplayName() {
        return sentenceCasedName(this);
    }
}
