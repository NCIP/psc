package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public abstract class DatedScheduledEventState extends ScheduledEventState {
    private Date date;

    public DatedScheduledEventState() { }

    public DatedScheduledEventState(String reason, Date date) {
        super(reason);
        this.date = date;
    }

    @Column(name = "actual_date")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
