package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * This enumeration represents the loading status. <p/> It is mainly used by classes that are loaded into <p/> PSC system, via grid service.
 * 
 * @author Saurabh Agrawal
 */

public enum LoadStatus {

	INPROGRESS(0),

	COMPLETE(1);

	int code;

	private LoadStatus(final int code) {

		this.code = code;

	}

	public int getCode() {

		return code;

	}

}
