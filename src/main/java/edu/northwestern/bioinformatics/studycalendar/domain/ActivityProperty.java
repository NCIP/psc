package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author Jalpa Patel
 */
@Entity
@Table(name = "activity_properties")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_activity_properties_id")
                }
)
public class ActivityProperty extends AbstractMutableDomainObject {
    private String namespace;
    private String name;
    private String value;
    private Activity activity;

    //BEAN PROPERTIES
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    @Column(name = "namespace")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
