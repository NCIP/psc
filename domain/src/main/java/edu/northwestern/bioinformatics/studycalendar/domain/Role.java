package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import gov.nih.nci.cabig.ctms.domain.EnumHelper;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.*;
import org.acegisecurity.GrantedAuthority;

@Deprecated
public enum Role implements CodedEnum<String>, GrantedAuthority {
    STUDY_COORDINATOR   (false),
    STUDY_ADMIN         (false),
    SYSTEM_ADMINISTRATOR(false),
    SUBJECT_COORDINATOR ,
    SITE_COORDINATOR    ;

    private boolean siteSpecific;

    private Role(boolean siteSpecific) {
        this.siteSpecific = siteSpecific;
        register(this);
    }

    private Role() {
        this(true);
    }

    public String getCode(){
        return name();
    }

    public String getDisplayName(){
        return EnumHelper.sentenceCasedName(this);
    }

    public static Role getByCode(String code) {
          return getByClassAndCode(Role.class, code);
    }

    public String getAuthority() {
        return name();
    }

    public String csmGroup() {
        return name();
    }

    public boolean isSiteSpecific() {
        return siteSpecific;
    }
}
