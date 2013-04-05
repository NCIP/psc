/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

public abstract class XMLValidator {
    private static Logger log = LoggerFactory.getLogger(XMLValidator.class);

    public static final SpringXMLValidator SPRING_ACTIVITY_VALIDATOR_INSTANCE = new SpringXMLValidator(Schema.activities);
    public static final SpringXMLValidator SPRING_TEMPLATE_VALIDATOR_INSTANCE = new SpringXMLValidator(Schema.template);

    public static final BasicXMLValidator BASIC_TEMPLATE_VALIDATOR_INSTANCE = new BasicXMLValidator(Schema.template);

    private Schema schema;

    public XMLValidator(Schema schema) {
        this.schema = schema;
    }

    protected void doValidate(Object o) throws SAXException, IOException {
        InputStream input = (InputStream) o;

        // 1. Specify you want a factory for XSD
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Load the specific schema you want.
        File uncompiledSchema = schema.file();

        log.debug("Validating against {}", uncompiledSchema.getAbsolutePath());

        // 3. Compile the schema.
        javax.xml.validation.Schema schema = factory.newSchema(this.schema.url());

        // 4. Get a validator from the schema.
        javax.xml.validation.Validator validator = schema.newValidator();
        log.debug("Validating with {}", validator.getClass().getName());

        // 5. Parse the document you want to check.
        Source source = new StreamSource(input);

        // 6. Check the document
        validator.validate(source);
        log.debug("{} file is valid.", getSchemaTitle());
    }

    protected String getSchemaTitle() {
        return schema.title();
    }

    private static String[] toStringArray(String... values) {
        return values;
    }

    public static class BasicXMLValidator extends XMLValidator {
        protected BasicXMLValidator(Schema schema) {
            super(schema);
        }

        public String validate(Object o) {
            try {
                doValidate(o);
            } catch (SAXException e) {
                log.debug("{} file is not valid because {}", getSchemaTitle(), e.getMessage());
                return MessageFormat.format("{0} file format is incorrect. {1}", toStringArray(getSchemaTitle()), "Error reading file");
            } catch (IOException e) {
                log.debug("Error reading file {} because {}", getSchemaTitle(), e.getMessage());
                return MessageFormat.format("There was a problem reading the {0} file.  Please try again.", getSchemaTitle(), e.getMessage());
            }
            return null;
        }
    }

    public static class SpringXMLValidator extends XMLValidator implements Validator {
        protected SpringXMLValidator(Schema schema) {
            super(schema);
        }

        public boolean supports(Class aClass) {
            return InputStream.class.isAssignableFrom(aClass);
        }

        public void validate(Object o, Errors errors) {
            try {
                doValidate(o);
            }
            catch (SAXException ex) {
                // TODO: get cause of SaxException and display in form other than cryptic SaxException message.
                errors.reject("error.file.not.valid", toStringArray(getSchemaTitle(), ex.getMessage()), "File not valid");
                log.debug("{} file is not valid because {}", getSchemaTitle(), ex.getMessage());
            }
            catch (IOException ioe) {
                errors.reject("error.problem.reading.file", toStringArray(getSchemaTitle()), "Error reading file");
                log.debug("Error reading file {} because {}", getSchemaTitle(), ioe.getMessage());
            }
        }
    }
}
