package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.math.IntRange;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.FetchType;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "arms")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_arms_id")
    }
)
public class Arm extends AbstractDomainObject {
    private Epoch epoch;
    private String name;
    private Set<Period> periods = new HashSet<Period>();

    // business methods

    public void addPeriod(Period period) {
        periods.add(period);
        period.setArm(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Period period : periods) {
            len = Math.max(period.getEndDay(), len);
        }
        return len;
    }

    @Transient
    public IntRange getDayRange() {
        return new IntRange(1, getLengthInDays());
    }

    // bean methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "epoch_id", nullable = false)
    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    @OneToMany (mappedBy = "arm")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(Set<Period> periods) {
        this.periods = periods;
    }
}
