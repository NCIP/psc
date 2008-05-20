package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.getByClassAndCode;
import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.register;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Saurabh Agrawal
 */
public enum Gender implements CodedEnum<String> {

    NOT_REPORTED("Not Reported"),
    UNKNOWN("Unknown"),
    FEMALE("Female"),
    MALE("Male");

    private String displayName;

    private Gender(final String displayName) {
        this.displayName = displayName;
        register(this);

    }

    public String getCode() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Map<String, String> getGenderMap() {
        Map<String, String> genders = new TreeMap<String, String>();

        genders.put(Gender.MALE.getCode(), Gender.MALE.getDisplayName());
        genders.put(Gender.FEMALE.getCode(), Gender.FEMALE.getDisplayName());
        genders.put(Gender.NOT_REPORTED.getCode(), Gender.NOT_REPORTED.getDisplayName());
        genders.put(Gender.UNKNOWN.getCode(), Gender.UNKNOWN.getDisplayName());

        return genders;
    }

    public static Gender getByCode(String code) {
        return getByClassAndCode(Gender.class, code);
    }
}

