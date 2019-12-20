//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 03:54:28 PM NZST 
//


package redhorizon.xml.units;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SpecialAnims.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SpecialAnims">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FACTORY_BUILD"/>
 *     &lt;enumeration value="FIRING"/>
 *     &lt;enumeration value="HARVESTER_DROPOFF"/>
 *     &lt;enumeration value="HARVESTER_HARVESTING"/>
 *     &lt;enumeration value="MOVING"/>
 *     &lt;enumeration value="SILO_FILL_25%"/>
 *     &lt;enumeration value="SILO_FILL_50%"/>
 *     &lt;enumeration value="SILO_FILL_75%"/>
 *     &lt;enumeration value="SILO_FILL_100%"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SpecialAnims")
@XmlEnum
public enum XMLSpecialAnims {

    FACTORY_BUILD("FACTORY_BUILD"),
    FIRING("FIRING"),
    HARVESTER_DROPOFF("HARVESTER_DROPOFF"),
    HARVESTER_HARVESTING("HARVESTER_HARVESTING"),
    MOVING("MOVING"),
    @XmlEnumValue("SILO_FILL_25%")
    SILO_FILL_25("SILO_FILL_25%"),
    @XmlEnumValue("SILO_FILL_50%")
    SILO_FILL_50("SILO_FILL_50%"),
    @XmlEnumValue("SILO_FILL_75%")
    SILO_FILL_75("SILO_FILL_75%"),
    @XmlEnumValue("SILO_FILL_100%")
    SILO_FILL_100("SILO_FILL_100%");
    private final String value;

    XMLSpecialAnims(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static XMLSpecialAnims fromValue(String v) {
        for (XMLSpecialAnims c: XMLSpecialAnims.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
