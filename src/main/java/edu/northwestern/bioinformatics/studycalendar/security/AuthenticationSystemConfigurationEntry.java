package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "authentication_system_configuration")
public class AuthenticationSystemConfigurationEntry extends ConfigurationEntry { }
