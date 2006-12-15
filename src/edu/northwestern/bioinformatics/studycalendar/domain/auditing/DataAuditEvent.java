package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "audit_events")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_audit_events_id")
    }
)
public class DataAuditEvent extends AbstractDomainObject {
    private DataAuditInfo info = new DataAuditInfo();
    private DataReference reference = new DataReference();
    private Operation operation;
    private String url;
    private List<DataAuditEventValue> values = new ArrayList<DataAuditEventValue>();

    /* for Hibernate */
    protected DataAuditEvent() { }

    public DataAuditEvent(DomainObject object, Operation operation) {
        this.reference = DataReference.create(object);
        this.operation = operation;
    }

    ////// LOGIC

    public void addValue(DataAuditEventValue value) {
        value.setAuditEvent(this);
        getValues().add(value);
    }

    ////// BEAN PROPERTIES

    @Enumerated(EnumType.STRING)
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="username", column = @Column(name = "username")),
        @AttributeOverride(name="ip", column = @Column(name = "ip_address")),
        @AttributeOverride(name="time", column = @Column(name = "time"))
    })
    public DataAuditInfo getInfo() {
        return info;
    }

    public void setInfo(DataAuditInfo info) {
        this.info = info;
    }

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="className", column = @Column(name = "object_class")),
        @AttributeOverride(name="id", column = @Column(name = "object_id"))
    })
    public DataReference getReference() {
        return reference;
    }

    public void setReference(DataReference reference) {
        this.reference = reference;
    }

    @OneToMany(mappedBy = "auditEvent")
    @OrderBy // order by ID for testing consistency
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<DataAuditEventValue> getValues() {
        return values;
    }

    public void setValues(List<DataAuditEventValue> values) {
        this.values = values;
    }
}
