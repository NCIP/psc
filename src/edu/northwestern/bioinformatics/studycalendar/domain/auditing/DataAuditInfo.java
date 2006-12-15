package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import org.hibernate.annotations.Type;

import javax.persistence.Embeddable;
import java.util.Date;
import java.sql.Timestamp;

/**
 * Subclass of core-commons' DataAuditInfo that aliases the "on" property as "time".
 * This is so that it can be used in HQL queries ("on" is apparently a reserved word).
 *
 * Also adds "url" property.
 *
 * @author Rhett Sutphin
 */
public class DataAuditInfo extends edu.nwu.bioinformatics.commons.DataAuditInfo {
    private String url;

    public DataAuditInfo() { }

    public DataAuditInfo(String by, String ip) {
        super(by, ip);
    }

    public DataAuditInfo(String by, String ip, Date on) {
        super(by, ip, on);
    }

    public DataAuditInfo(String by, String ip, Date on, String url) {
        super(by, ip, on);
        this.url = url;
    }

    public static DataAuditInfo copy(edu.nwu.bioinformatics.commons.DataAuditInfo source) {
        DataAuditInfo copy = new DataAuditInfo(
            source.getBy(), source.getIp(), source.getOn()
        );
        if (source instanceof DataAuditInfo) {
            copy.setUrl(((DataAuditInfo) source).getUrl());
        }
        return copy;
    }

    public Date getTime() {
        return getOn();
    }

    public void setTime(Date on) {
        setOn(on);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /* Have to duplicate other accessors so that it will work with hibernate annotations without
       annotating the superclass. */
    public String getUsername() { return super.getBy(); }
    public void setUsername(String by) { super.setBy(by); }
    public String getIp() { return super.getIp(); }
    public void setIp(String ip) { super.setIp(ip); }
}
