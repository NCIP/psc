package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import gov.nih.nci.cabig.ctms.domain.EnumHelper;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.*;


public enum Role implements CodedEnum<String> {
    STUDY_COORDINATOR       ("STUDY_COORDINATOR", false  ),
    STUDY_ADMIN             ("STUDY_ADMIN",       false  ),
    PARTICIPANT_COORDINATOR ("PARTICIPANT_COORDINATOR"  ),
    RESEARCH_ASSOCIATE      ("RESEARCH_ASSOCIATE"       ),
    SITE_COORDINATOR        ("SITE_COORDINATOR"         );

    private String csmName;
    private boolean siteSpecific;

    private Role(String name, boolean siteSpecific) {
        this.csmName = name;
        this.siteSpecific = siteSpecific;
        register(this);
    }

    private Role(String name) {
        this(name, true);
    }


    public String getCode(){
        return csmName;
    }

    public String getDisplayName(){
        return EnumHelper.sentenceCasedName(this);
    }

    public static Role getByCode(String code) {
          return getByClassAndCode(Role.class, code);
    }

    public String toString() {
        return csmName;
    }

    public String csmGroup() {
        return csmName;
    }

    public boolean isSiteSpecific() {
        return siteSpecific;
    }

    public static final String[] strValues() {
        Role[] roles = Role.values();
        String[] strRoles= new String[roles.length];
        for(int i=0; i < roles.length; i++) {
            strRoles[i] = roles[i].toString();
        }
        return strRoles;
    }
}
