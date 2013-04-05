/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

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
