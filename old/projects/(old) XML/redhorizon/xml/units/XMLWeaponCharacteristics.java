//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 03:54:28 PM NZST 
//


package redhorizon.xml.units;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WeaponCharacteristics.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="WeaponCharacteristics">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FULL_SALVO_FIRING"/>
 *     &lt;enumeration value="MUST_STOP_TO_FIRE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "WeaponCharacteristics")
@XmlEnum
public enum XMLWeaponCharacteristics {

    FULL_SALVO_FIRING,
    MUST_STOP_TO_FIRE;

    public String value() {
        return name();
    }

    public static XMLWeaponCharacteristics fromValue(String v) {
        return valueOf(v);
    }

}
