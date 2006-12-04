package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
//import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */
public class ReportRowDao extends HibernateDaoSupport { 
	private static final Logger log = Logger.getLogger(ReportRowDao.class.getName());
	
	public List<ReportRow> getAll(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery myQuery = (SQLQuery) session.getNamedQuery("reportSqlQuery");	        
	            List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 

	public List<ReportRow> getFilteredByDates(){

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
	
	public List<ReportRow> getFilteredByName(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.enableFilter("testEventName").setParameter("name", "Urinalysis");
				SQLQuery myQuery = (SQLQuery) session.getNamedQuery("reportSqlQuery");
				List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 

}
