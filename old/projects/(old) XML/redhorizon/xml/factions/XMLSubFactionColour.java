//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 03:54:28 PM NZST 
//


package redhorizon.xml.factions;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SubFactionColour.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SubFactionColour">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GOLD"/>
 *     &lt;enumeration value="BLUE"/>
 *     &lt;enumeration value="RED"/>
 *     &lt;enumeration value="GREEN"/>
 *     &lt;enumeration value="ORANGE"/>
 *     &lt;enumeration value="BROWN"/>
 *     &lt;enumeration value="TEAL"/>
 *     &lt;enumeration value="MAROON"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SubFactionColour")
@XmlEnum
public enum XMLSubFactionColour {

    GOLD,
    BLUE,
    RED,
    GREEN,
    ORANGE,
    BROWN,
    TEAL,
    MAROON;

    public String value() {
        return name();
    }

    public static XMLSubFactionColour fromValue(String v) {
        return valueOf(v);
    }

}
