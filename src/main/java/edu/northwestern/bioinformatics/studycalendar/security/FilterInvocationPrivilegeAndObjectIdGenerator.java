package edu.northwestern.bioinformatics.studycalendar.security;

import org.acegisecurity.intercept.web.FilterInvocation;

public class FilterInvocationPrivilegeAndObjectIdGenerator extends
		RegexPrivilegeAndObjectIdGenerator {

	protected boolean supports(Object object){
		return object instanceof FilterInvocation;
	}

	protected String getKeyValue(Object object){
		return ((FilterInvocation)object).getRequestUrl();
	}

}
