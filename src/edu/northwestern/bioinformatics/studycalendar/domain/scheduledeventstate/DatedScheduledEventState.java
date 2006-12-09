package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.text.SimpleDateFormat;

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

    @Override
    protected void appendSummaryMiddle(StringBuilder sb) {
        sb.append(' ');
        appendPreposition(sb);
        sb.append(' ');
        // TODO: centrally configure date format
        sb.append(new SimpleDateFormat("M/d/yyyy").format(getDate()));
    }

    protected abstract void appendPreposition(StringBuilder sb);

    ////// BEAN PROPERTIES

    @Column(name = "actual_date")
    @Temporal(TemporalType.DATE)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    ////// OBJECT METHODS

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DatedScheduledEventState that = (DatedScheduledEventState) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
