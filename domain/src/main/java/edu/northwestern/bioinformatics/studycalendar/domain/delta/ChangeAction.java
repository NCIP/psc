/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.*;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */

public enum ChangeAction implements CodedEnum<String> {
    ADD,
    REMOVE,
    REORDER,
    CHANGE_PROPERTY("property")
    ;

    //action = add, remove, reorder
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
