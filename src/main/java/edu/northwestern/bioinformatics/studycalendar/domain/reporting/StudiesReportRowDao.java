package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

/**
 * @author John Dzak
 */
public class StudiesReportRowDao extends ReportDao<StudiesReportRow>{

    public Class<StudiesReportRow> domainClass() {
        return StudiesReportRow.class;
    }
}
