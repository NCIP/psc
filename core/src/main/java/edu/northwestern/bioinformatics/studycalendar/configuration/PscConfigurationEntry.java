/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "configuration")
@AttributeOverride(name = "key", column = @Column(name = "prop"))
public class PscConfigurationEntry extends ConfigurationEntry { }
