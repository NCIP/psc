package edu.northwestern.bioinformatics.studycalendar.xml;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Aug 20, 2008
 * Time: 10:20:53 AM
 * To change this template use File | Settings | File Templates.
 */
public interface AbstractCsvXlsSerializer<R> {

    /**
     * Create a document for the given object
     * @return string of CSV.
     * @param  object to create the CVS
     */
    String createDocumentString(R object, String delimiter);

    String constructHeader(String delimiter);

}
