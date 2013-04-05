/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.felixcm;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Vector;

import static edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.OsgiConfigurationProperty.CollectionType.*;
import static edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.OsgiConfigurationProperty.Type.*;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({"UnnecessaryBoxing"})
public class OsgiConfigurationPropertyTest extends TestCase {
    private OsgiConfigurationProperty property;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        property = new OsgiConfigurationProperty();
    }

    ////// Type ENUM TESTS
    
    public void testTypeNameForPrimitives() throws Exception {
        assertEquals("long", PRIMITIVE_LONG.typeName());
        assertEquals("int", PRIMITIVE_INT.typeName());
        assertEquals("short", PRIMITIVE_SHORT.typeName());
        assertEquals("byte", PRIMITIVE_BYTE.typeName());

        assertEquals("double", PRIMITIVE_DOUBLE.typeName());
        assertEquals("float", PRIMITIVE_FLOAT.typeName());

        assertEquals("char", PRIMITIVE_CHAR.typeName());
        assertEquals("boolean", PRIMITIVE_BOOLEAN.typeName());
    }

    public void testTypeNameForSimples() throws Exception {
        assertEquals("Long", LONG.typeName());
        assertEquals("Integer", INTEGER.typeName());
        assertEquals("Short", SHORT.typeName());
        assertEquals("Byte", BYTE.typeName());

        assertEquals("Double", DOUBLE.typeName());
        assertEquals("Float", FLOAT.typeName());

        assertEquals("Character", CHARACTER.typeName());
        assertEquals("Boolean", BOOLEAN.typeName());
        assertEquals("String", STRING.typeName());
    }

    public void testFromTypeNameForPrimitives() {
        assertEquals(PRIMITIVE_LONG,    OsgiConfigurationProperty.Type.fromTypeName("long"));
        assertEquals(PRIMITIVE_INT,     OsgiConfigurationProperty.Type.fromTypeName("int"));
        assertEquals(PRIMITIVE_SHORT,   OsgiConfigurationProperty.Type.fromTypeName("short"));
        assertEquals(PRIMITIVE_BYTE,    OsgiConfigurationProperty.Type.fromTypeName("byte"));

        assertEquals(PRIMITIVE_DOUBLE,  OsgiConfigurationProperty.Type.fromTypeName("double"));
        assertEquals(PRIMITIVE_FLOAT,   OsgiConfigurationProperty.Type.fromTypeName("float"));

        assertEquals(PRIMITIVE_CHAR,    OsgiConfigurationProperty.Type.fromTypeName("char"));
        assertEquals(PRIMITIVE_BOOLEAN, OsgiConfigurationProperty.Type.fromTypeName("boolean"));
    }

    public void testFromTypeNameForSimples() {
        assertEquals(LONG,      OsgiConfigurationProperty.Type.fromTypeName("Long"));
        assertEquals(INTEGER,   OsgiConfigurationProperty.Type.fromTypeName("Integer"));
        assertEquals(SHORT,     OsgiConfigurationProperty.Type.fromTypeName("Short"));
        assertEquals(BYTE,      OsgiConfigurationProperty.Type.fromTypeName("Byte"));

        assertEquals(DOUBLE,    OsgiConfigurationProperty.Type.fromTypeName("Double"));
        assertEquals(FLOAT,     OsgiConfigurationProperty.Type.fromTypeName("Float"));

        assertEquals(CHARACTER, OsgiConfigurationProperty.Type.fromTypeName("Character"));
        assertEquals(BOOLEAN,   OsgiConfigurationProperty.Type.fromTypeName("Boolean"));
    }

    public void testFromJavaClassForPrimitives() {
        assertEquals(PRIMITIVE_LONG,    OsgiConfigurationProperty.Type.fromJavaClass(long.class));
        assertEquals(PRIMITIVE_INT,     OsgiConfigurationProperty.Type.fromJavaClass(int.class));
        assertEquals(PRIMITIVE_SHORT,   OsgiConfigurationProperty.Type.fromJavaClass(short.class));
        assertEquals(PRIMITIVE_BYTE,    OsgiConfigurationProperty.Type.fromJavaClass(byte.class));

        assertEquals(PRIMITIVE_DOUBLE,  OsgiConfigurationProperty.Type.fromJavaClass(double.class));
        assertEquals(PRIMITIVE_FLOAT,   OsgiConfigurationProperty.Type.fromJavaClass(float.class));

        assertEquals(PRIMITIVE_CHAR,    OsgiConfigurationProperty.Type.fromJavaClass(char.class));
        assertEquals(PRIMITIVE_BOOLEAN, OsgiConfigurationProperty.Type.fromJavaClass(boolean.class));
    }

    public void testFromJavaClassForSimples() {
        assertEquals(LONG,      OsgiConfigurationProperty.Type.fromJavaClass(Long.class));
        assertEquals(INTEGER,   OsgiConfigurationProperty.Type.fromJavaClass(Integer.class));
        assertEquals(SHORT,     OsgiConfigurationProperty.Type.fromJavaClass(Short.class));
        assertEquals(BYTE,      OsgiConfigurationProperty.Type.fromJavaClass(Byte.class));

        assertEquals(DOUBLE,    OsgiConfigurationProperty.Type.fromJavaClass(Double.class));
        assertEquals(FLOAT,     OsgiConfigurationProperty.Type.fromJavaClass(Float.class));

        assertEquals(CHARACTER, OsgiConfigurationProperty.Type.fromJavaClass(Character.class));
        assertEquals(BOOLEAN,   OsgiConfigurationProperty.Type.fromJavaClass(Boolean.class));
    }

    public void testFromTypeNameForUnknown() throws Exception {
        assertNull(OsgiConfigurationProperty.Type.fromTypeName("Color"));
    }

    public void testConvertPrimitiveYieldsBoxedInstance() throws Exception {
        assertEquals( 7L, PRIMITIVE_LONG.convert("7"));
        assertEquals(  8, PRIMITIVE_INT.convert("8"));
        assertEquals((short) 9, PRIMITIVE_SHORT.convert("9"));
        assertEquals((byte) 10, PRIMITIVE_BYTE.convert("10"));

        assertEquals(1.0, PRIMITIVE_DOUBLE.convert("1"));
        assertEquals( 2f, PRIMITIVE_FLOAT.convert("2"));

        assertEquals('J',  PRIMITIVE_CHAR.convert("J"));
        assertEquals(true, PRIMITIVE_BOOLEAN.convert("true"));
    }

    public void testConvertSimple() throws Exception {
        assertEquals( 7L, LONG.convert("7"));
        assertEquals(  8, INTEGER.convert("8"));
        assertEquals((short) 9, SHORT.convert("9"));
        assertEquals((byte) 10, BYTE.convert("10"));

        assertEquals(1.0, DOUBLE.convert("1"));
        assertEquals( 2f, FLOAT.convert("2"));

        assertEquals('J',  CHARACTER.convert("J"));
        assertEquals(true, BOOLEAN.convert("true"));
        assertEquals("any", STRING.convert("any"));
    }

    public void testCreateArray() {
        Object actual = PRIMITIVE_DOUBLE.createArray(7);
        assertNotNull("No array created", actual);
        assertTrue("Wrong type", actual instanceof double[]);
        assertEquals("Wrong size", 7, ((double[]) actual).length);
    }
    
    ////// setValue TESTS

    public void testSetValueFromSingleString() {
        property.setValue("false");
        assertPropertyAttributes(STRING, SINGLE, "false");
    }

    public void testSetValueFromSingleWrappedLong() {
        property.setValue(new Long(7L));
        assertPropertyAttributes(LONG, SINGLE, "7");
    }

    public void testSetValueFromSingleWrappedInteger() {
        property.setValue(new Integer(8));
        assertPropertyAttributes(INTEGER, SINGLE, "8");
    }

    public void testSetValueFromSingleWrappedShort() {
        property.setValue(new Short((short) 9));
        assertPropertyAttributes(SHORT, SINGLE, "9");
    }

    public void testSetValueFromSingleWrappedByte() {
        property.setValue(new Byte((byte) 10));
        assertPropertyAttributes(BYTE, SINGLE, "10");
    }

    public void testSetValueFromSingleWrappedDouble() {
        property.setValue(new Double(0.9));
        assertPropertyAttributes(DOUBLE, SINGLE, "0.9");
    }

    public void testSetValueFromSingleWrappedFloat() {
        property.setValue(new Float(0.09f));
        assertPropertyAttributes(FLOAT, SINGLE, "0.09");
    }

    public void testSetValueFromSingleWrappedChar() {
        property.setValue(new Character('r'));
        assertPropertyAttributes(CHARACTER, SINGLE, "r");
    }

    public void testSetValueFromSingleWrappedBoolean() {
        property.setValue(Boolean.TRUE);
        assertPropertyAttributes(BOOLEAN, SINGLE, "true");
    }

    public void testSetValueFromPrimitiveArray() throws Exception {
        property.setValue(new float[] { 0.3f, 1.0f, 1.2f });
        assertPropertyAttributes(PRIMITIVE_FLOAT, ARRAY, "0.3", "1.0", "1.2");
    }

    public void testSetValueFromSimpleArray() throws Exception {
        property.setValue(new Character[] { 'S', '?', '7' });
        assertPropertyAttributes(CHARACTER, ARRAY, "S", "?", "7");
    }

    public void testSetValueFromVector() throws Exception {
        property.setValue(new Vector<Integer>(Arrays.asList(25, 16, 9, 4, 1)));
        assertPropertyAttributes(INTEGER, VECTOR, "25", "16", "9", "4", "1");
    }

    public void testSetValueFromEmptyVector() throws Exception {
        property.setValue(new Vector<Integer>());
        assertPropertyAttributes(STRING, VECTOR);
    }

    ////// getValue TESTS
    
    public void testGetValueForPrimitiveArray() throws Exception {
        setPropertyAttributes(PRIMITIVE_SHORT, ARRAY, "256", "13", "-150");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof short[]);
        short[] actual = (short[]) value;
        assertEquals("Wrong number of values", 3, actual.length);
        assertEquals("Wrong value 0", 256, actual[0]);
        assertEquals("Wrong value 1", 13, actual[1]);
        assertEquals("Wrong value 2", -150, actual[2]);
    }

    public void testGetValueForSimpleArray() throws Exception {
        setPropertyAttributes(FLOAT, ARRAY, "256", "13", "-150");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof Float[]);
        Float[] actual = (Float[]) value;
        assertEquals("Wrong number of values", 3, actual.length);
        assertEquals("Wrong value 0", 256.0f, actual[0]);
        assertEquals("Wrong value 1", 13.0f, actual[1]);
        assertEquals("Wrong value 2", -150.0f, actual[2]);
    }

    public void testGetValueForStringArray() throws Exception {
        setPropertyAttributes(STRING, ARRAY, "256", "13", "-150");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof String[]);
        String[] actual = (String[]) value;
        assertEquals("Wrong number of values", 3, actual.length);
        assertEquals("Wrong value 0", "256", actual[0]);
        assertEquals("Wrong value 1", "13", actual[1]);
        assertEquals("Wrong value 2", "-150", actual[2]);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void testGetValueForSimpleVector() throws Exception {
        setPropertyAttributes(BYTE, VECTOR, "25", "55", "-75");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof Vector);
        Vector actual = (Vector) value;
        assertEquals("Wrong number of values", 3, actual.size());
        assertTrue("Wrong value type", actual.get(0) instanceof Byte);
        assertEquals("Wrong value 0", (byte) 25, actual.get(0));
        assertEquals("Wrong value 1", (byte) 55, actual.get(1));
        assertEquals("Wrong value 2", (byte) -75, actual.get(2));
    }

    public void testGetValueForSingleSimple() throws Exception {
        setPropertyAttributes(LONG, SINGLE, "78");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof Long);
        assertEquals("Wrong value", 78L, value);
    }

    public void testGetValueForSingleBoolean() throws Exception {
        setPropertyAttributes(BOOLEAN, SINGLE, "true");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof Boolean);
        assertEquals("Wrong value", true, value);
    }

    public void testGetValueForSingleString() throws Exception {
        setPropertyAttributes(STRING, SINGLE, "78");
        Object value = property.getValue();
        assertTrue("Wrong type", value instanceof String);
        assertEquals("Wrong value", "78", value);
    }

    private void setPropertyAttributes(
        OsgiConfigurationProperty.Type type, 
        OsgiConfigurationProperty.CollectionType collectionType, 
        String... rawValues
    ) {
        property.setType(type);
        property.setCollectionType(collectionType);
        property.getRawValues().clear();
        for (String rawValue : rawValues) {
            property.getRawValues().add(rawValue);
        }
    }
    
    private void assertPropertyAttributes(
        OsgiConfigurationProperty.Type expectedType,
        OsgiConfigurationProperty.CollectionType expectedCollectionType,
        String... expectedRawValues
    ) {
        assertEquals("Wrong type", expectedType, property.getType());
        assertEquals("Wrong collection type", expectedCollectionType, property.getCollectionType());
        assertEquals("Wrong number of values",
            expectedRawValues.length, property.getRawValues().size());
        for (int i = 0; i < expectedRawValues.length; i++) {
            assertEquals("Wrong raw value " + i, expectedRawValues[i], property.getRawValues().get(i));
        }
    }
}
