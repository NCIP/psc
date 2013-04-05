/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import gov.nih.nci.cabig.ctms.tools.hibernate.CodedEnumType;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringCodedEnumType extends CodedEnumType {

    @Override
    protected int codeSqlType() {
        return Types.VARCHAR;
    }

    @Override
    protected Class codeJavaType() {
        return String.class;
    }

    @Override
    protected Object getKeyObject(ResultSet rs, String colname) throws SQLException {
        return rs.getString(colname);
    }    
}
