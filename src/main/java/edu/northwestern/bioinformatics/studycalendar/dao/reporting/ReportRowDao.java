package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
//import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */
public class ReportRowDao extends HibernateDaoSupport { 
	private static final Logger log = LoggerFactory.getLogger(ReportRowDao.class.getName());
	
	@SuppressWarnings("unchecked")
	public List<ReportRow> getAll(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(final Session session) throws HibernateException, SQLException {
				
				String query = "select " +
				"se.id as event_id," + 
				"act.name as event_name, " +
				"sem.name as state_name, " +
				"se.current_state_date as date," +
				"se.ideal_date as ideal_date," +
				"a.name as study_segment_name," +
				"e.name as epoch_name, " +
				"p.first_name as p_first_name, " +
				"p.last_name as p_last_name, " + 
				"st.name as study_name, " +
				"si.name as site_name "+
			"from "+
				"sites si inner join study_sites ss on si.id=ss.site_id " +
				"inner join studies st on st.id=ss.study_id " +
				"inner join subject_assignments sas on ss.id=pa.study_site_id " +
				"inner join subjects s on sas.subject_id=p.id " +
				"inner join scheduled_calendars sc on sas.id=sc.assignment_id " +
				"inner join scheduled_study_segments sa on sc.id=sa.scheduled_calendar_id " +
				"inner join scheduled_activities sac on sa.id=se.scheduled_study_segment_id " +
				"inner join scheduled_activity_modes sam on sac.current_state_mode_id=sem.id " +
				"inner join planned_activities pa on sac.planned_activity_id=pe.id " +
				"inner join activities act on pa.activity_id=act.id " +
				"inner join study_segments a on sa.study_segment_id=a.id " +
				"inner join epochs e on a.epoch_id=e.id " +
			"order by se.id";
				
				SQLQuery myQuery = session.createSQLQuery(query);
				myQuery.setResultSetMapping("reportSqlQueryMapping");
	            final List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 

/*	public List<ReportRow> getFilteredByDates(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Calendar from = new GregorianCalendar(2007, 01, 01);
				Calendar to = new GregorianCalendar(2007, 02, 01);		
				Filter dateFilter = session.enableFilter("betweenDates").setParameter("fromDate", from.getTime());
				dateFilter.setParameter("toDate", to.getTime());
				SQLQuery myQuery = (SQLQuery) session.getNamedQuery("reportSqlQuery");
				List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 
*/
	@SuppressWarnings("unchecked")
	public List<ReportRow> getFilteredReport(List<Site> sites, List<Study> studies, List<Subject> subjects, String startDate, String endDate) {
		final List<Site> sitesFinal = sites;
		final List<Study> studiesFinal = studies;
		final List<Subject> subjectsFinal = subjects;
		final String startDateFinal = startDate;
		final String endDateFinal = endDate;
		
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String query = "select " +
				"se.id as event_id," + 
				"act.name as event_name, " +
				"sem.name as state_name, " +
				"se.current_state_date as date," +
				"se.ideal_date as ideal_date," +
				"a.name as study_segment_name," +
				"e.name as epoch_name, " +
				"p.first_name as p_first_name, " +
				"p.last_name as p_last_name, " + 
				"st.name as study_name, " +
				"si.name as site_name "+
			"from "+
				"sites si inner join study_sites ss on si.id=ss.site_id " +
				"inner join studies st on st.id=ss.study_id " +
				"inner join subject_assignments sas on ss.id=pa.study_site_id " +
				"inner join subjects s on sas.subject_id=p.id " +
				"inner join scheduled_calendars sc on sas.id=sc.assignment_id " +
				"inner join scheduled_study_segments sa on sc.id=sa.scheduled_calendar_id " +
				"inner join scheduled_activities sac on sa.id=se.scheduled_study_segment_id " +
				"inner join scheduled_activity_modes sam on sac.current_state_mode_id=sem.id " +
				"inner join planned_activities pa on sac.planned_activity_id=pe.id " +
				"inner join activities act on pa.activity_id=act.id " +
				"inner join study_segments a on sa.study_segment_id=a.id " +
				"inner join epochs e on a.epoch_id=e.id " +
				getWhere(sitesFinal, studiesFinal, subjectsFinal, startDateFinal, endDateFinal) +
			" order by se.id";
				
				log.debug("%%%%%%%%%%%%" + query.toString());
				SQLQuery myQuery = session.createSQLQuery(query);
				myQuery.setResultSetMapping("reportSqlQueryMapping");
	            List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	}
	
	private String getWhere(List<Site> sites, List<Study> studies, List<Subject> subjects, String startDate, String endDate) {
		String where = new String();
		
		List<String> wheres = new ArrayList<String>();
		if(!"".equals(getSitesWheres(sites))) { wheres.add(getSitesWheres(sites));}
		if(!"".equals(getStudiesWheres(studies))) { wheres.add(getStudiesWheres(studies));}
		if(!"".equals(getSubjectsWheres(subjects))) {wheres.add(getSubjectsWheres(subjects));}
		if(!"".equals(getDateWheres(startDate, endDate))) {wheres.add(getDateWheres(startDate, endDate));}
		
		if(wheres.isEmpty()) {
			return where;
		} else {
			where = "where ";
			ListIterator<String> iter = wheres.listIterator();
			while(iter.hasNext()) {
				String whereClause = iter.next();
				where += whereClause;
				if(iter.hasNext()) {
					if(!"".equals(iter.next())) {
						where += " and ";
						iter.previous();
					} else {
						iter.previous();					
					}
				}
			where += " ";
			}
		return where;
		}
	}
		

	private String getSitesWheres(List<Site> sites) {
		String where = new String();
		if(sites.isEmpty()) {
			return where;
		} else {
			where = "si.id IN (";
			ListIterator<Site> iter = sites.listIterator();
			while(iter.hasNext()) {
				Site site = iter.next();
				where += site.getId();
				if(iter.hasNext()) { where += ",";}
			}
			where += ") ";
		}
		return where;
	}


	private String getStudiesWheres(List<Study> studies) {
		String where = new String();
		if(studies.isEmpty()) {
			return where;
		} else {
			where = "st.id IN (";
			ListIterator<Study> iter = studies.listIterator();
			while(iter.hasNext()) {
				Study study = iter.next();
				where += study.getId();
				if(iter.hasNext()) { where += ",";}
			}
			where += ") ";
		}
		return where;
	}
	
	private String getSubjectsWheres(List<Subject> subjects) {
		String where = new String();
		if(subjects.isEmpty()) {
			return where;
		} else {
			where = "p.id IN (";
			ListIterator<Subject> iter = subjects.listIterator();
			while(iter.hasNext()) {
				Subject subject = iter.next();
				where += subject.getId();
				if(iter.hasNext()) { where += ",";}
			}
			where += ") ";
		}
		return where;
	}
	
	private String getDateWheres(String startDate, String endDate) {
		String where = new String();
		
		if("".equals(startDate) && "".equals(endDate)) {
			return where;
		} else if ("".equals(startDate) && (!"".equals(endDate))) {
			where = " se.current_state_date<='" + endDate + "'" ;
		} else if (!"".equals(startDate) && ("".equals(endDate))) {
			where = " se.current_state_date>='" + startDate + "'" ;
		} else {
			where = " se.current_state_date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
		}
		return where;
	}
	
}