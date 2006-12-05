package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
//import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */
public class ReportRowDao extends HibernateDaoSupport { 
	private static final Logger log = Logger.getLogger(ReportRowDao.class.getName());
	
	@SuppressWarnings("unchecked")
	public List<ReportRow> getAll(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(final Session session) throws HibernateException, SQLException {
				
				String query = "select " +
				"se.id as event_id," + 
				"act.name as event_name, " +
				"sem.name as state_name, " +
				"se.current_state_date as date," +
				"a.name as arm_name," +
				"e.name as epoch_name, " +
				"p.first_name as p_first_name, " +
				"p.last_name as p_last_name, " + 
				"st.name as study_name, " +
				"si.name as site_name "+
			"from "+
				"sites si inner join study_sites ss on si.id=ss.site_id " +
				"inner join studies st on st.id=ss.study_id " +
				"inner join participant_assignments pa on ss.id=pa.study_site_id " +
				"inner join participants p on pa.participant_id=p.id " +
				"inner join scheduled_calendars sc on pa.id=sc.assignment_id " +
				"inner join scheduled_arms sa on sc.id=sa.scheduled_calendar_id " +
				"inner join scheduled_events se on sa.id=se.scheduled_arm_id " +
				"inner join scheduled_event_modes sem on se.current_state_mode_id=sem.id " +
				"inner join planned_events pe on se.planned_event_id=pe.id " +
				"inner join activities act on pe.activity_id=act.id " +
				"inner join arms a on sa.arm_id=a.id " +
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
	public List<ReportRow> getFilteredReport(List<Site> sites, List<Study> studies, List<Participant> participants, String fromDate, String toDate) {
		final  List<Site> sitesFinal = sites;
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String query = "select " +
				"se.id as event_id," + 
				"act.name as event_name, " +
				"sem.name as state_name, " +
				"se.current_state_date as date," +
				"a.name as arm_name," +
				"e.name as epoch_name, " +
				"p.first_name as p_first_name, " +
				"p.last_name as p_last_name, " + 
				"st.name as study_name, " +
				"si.name as site_name "+
			"from "+
				"sites si inner join study_sites ss on si.id=ss.site_id " +
				"inner join studies st on st.id=ss.study_id " +
				"inner join participant_assignments pa on ss.id=pa.study_site_id " +
				"inner join participants p on pa.participant_id=p.id " +
				"inner join scheduled_calendars sc on pa.id=sc.assignment_id " +
				"inner join scheduled_arms sa on sc.id=sa.scheduled_calendar_id " +
				"inner join scheduled_events se on sa.id=se.scheduled_arm_id " +
				"inner join scheduled_event_modes sem on se.current_state_mode_id=sem.id " +
				"inner join planned_events pe on se.planned_event_id=pe.id " +
				"inner join activities act on pe.activity_id=act.id " +
				"inner join arms a on sa.arm_id=a.id " +
				"inner join epochs e on a.epoch_id=e.id " +
				"where " +
 				getSitesWheres(sitesFinal) + 
			"order by se.id";
				
				log.debug("%%%%%%%%%%%%" + query.toString());
				SQLQuery myQuery = session.createSQLQuery(query);
				myQuery.setResultSetMapping("reportSqlQueryMapping");
	            List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
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
}