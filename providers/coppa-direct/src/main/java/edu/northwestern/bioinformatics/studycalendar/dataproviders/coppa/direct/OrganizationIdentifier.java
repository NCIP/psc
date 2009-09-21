package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import gov.nih.nci.coppa.po.Id;
import org.iso._21090.II;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

public class OrganizationIdentifier {
    public static final String ORGANIZATION_II_ROOT = "2.16.840.1.113883.3.26.4.2";
    public String identifier;

    public OrganizationIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public static OrganizationIdentifier fromAssignedIdentifier(String val) {
        return new OrganizationIdentifier(val);
    }

    public Id createId() {
        return buildCoppaIdentifier(Id.class);
    }
    public II createII() {
        return buildCoppaIdentifier(II.class);
    }
    
    private <T extends org.iso._21090.II> T buildCoppaIdentifier(Class<T> clazz) {
        try {
            T inst = clazz.newInstance();
            inst.setRoot(ORGANIZATION_II_ROOT);
            inst.setExtension(identifier);
            return inst;
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Inaccessible child class", e);
        } catch (InstantiationException e ) {
            throw new StudyCalendarError("Uninstantiable child class", e);
        }
    }
}
