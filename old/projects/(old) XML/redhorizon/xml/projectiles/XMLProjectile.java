//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 03:54:28 PM NZST 
//


package redhorizon.xml.projectiles;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import redhorizon.xml.XMLExtendableObject;
import redhorizon.xml.media.XMLAnimation;


/**
 * <p>Java class for Projectile complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Projectile">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ultraq.net.nz/redhorizon/common}ExtendableObject">
 *       &lt;sequence>
 *         &lt;element name="Animation" type="{http://www.ultraq.net.nz/redhorizon/media}Animation" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Characteristics">
 *         &lt;simpleType>
 *           &lt;list itemType="{http://www.ultraq.net.nz/redhorizon/projectiles}CharsProjectiles" />
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Projectile", propOrder = {
    "animation"
})
public class XMLProjectile
    extends XMLExtendableObject
{

    @XmlElement(name = "Animation")
    protected XMLAnimation animation;
    @XmlAttribute(name = "Characteristics")
    protected List<XMLCharsProjectiles> characteristics;

    /**
     * Gets the value of the animation property.
     * 
     * @return
     *     possible object is
     *     {@link XMLAnimation }
     *     
     */
    public XMLAnimation getAnimation() {
        return animation;
    }

    /**
     * Sets the value of the animation property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLAnimation }
     *     
     */
    public void setAnimation(XMLAnimation value) {
        this.animation = value;
    }

    /**
     * Gets the value of the characteristics property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the characteristics property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCharacteristics().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLCharsProjectiles }
     * 
     * 
     */
    public List<XMLCharsProjectiles> getCharacteristics() {
        if (characteristics == null) {
            characteristics = new ArrayList<XMLCharsProjectiles>();
        }
        return this.characteristics;
    }

}
