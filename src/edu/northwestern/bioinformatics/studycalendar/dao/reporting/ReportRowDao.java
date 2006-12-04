package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

//import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

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
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery myQuery = (SQLQuery) session.getNamedQuery("reportSqlQuery");	        
	            List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 

	@SuppressWarnings("unchecked")
	public List<ReportRow> getFilteredTest(){

		return getHibernateTemplate().executeFind(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery myQuery = (SQLQuery) session.getNamedQuery("reportSqlQuery");
				
				List<ReportRow> reportRowList = myQuery.list();
	            return reportRowList;
			}
		});
		
	} 
}
