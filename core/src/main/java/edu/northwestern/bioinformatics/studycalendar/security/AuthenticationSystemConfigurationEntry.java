package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.AttributeOverride;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "authentication_system_conf")
@AttributeOverride(name = "key", column = @Column(name = "prop"))
public class AuthenticationSystemConfigurationEntry extends ConfigurationEntry { }
