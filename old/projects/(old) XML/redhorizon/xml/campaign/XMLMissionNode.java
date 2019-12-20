//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 03:54:28 PM NZST 
//


package redhorizon.xml.campaign;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import redhorizon.xml.XMLReferenceableObject;


/**
 * <p>Java class for MissionNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MissionNode">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ultraq.net.nz/redhorizon/common}ReferenceableObject">
 *       &lt;sequence>
 *         &lt;element name="Mission" type="{http://www.ultraq.net.nz/redhorizon/campaign}MissionRef" maxOccurs="unbounded"/>
 *         &lt;choice>
 *           &lt;element name="WinBranch" type="{http://www.ultraq.net.nz/redhorizon/campaign}MissionNodeRef"/>
 *           &lt;element name="LoseBranch" type="{http://www.ultraq.net.nz/redhorizon/campaign}MissionNodeRef"/>
 *           &lt;element name="Ending" type="{http://www.ultraq.net.nz/redhorizon/campaign}EndingRef"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MissionNode", propOrder = {
    "mission",
    "winBranch",
    "loseBranch",
    "ending"
})
public class XMLMissionNode
    extends XMLReferenceableObject
{

    @XmlElement(name = "Mission", required = true)
    protected List<XMLMissionRef> mission;
    @XmlElement(name = "WinBranch")
    protected XMLMissionNodeRef winBranch;
    @XmlElement(name = "LoseBranch")
    protected XMLMissionNodeRef loseBranch;
    @XmlElement(name = "Ending")
    protected XMLEndingRef ending;

    /**
     * Gets the value of the mission property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mission property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMission().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLMissionRef }
     * 
     * 
     */
    public List<XMLMissionRef> getMission() {
        if (mission == null) {
            mission = new ArrayList<XMLMissionRef>();
        }
        return this.mission;
    }

    /**
     * Gets the value of the winBranch property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMissionNodeRef }
     *     
     */
    public XMLMissionNodeRef getWinBranch() {
        return winBranch;
    }

    /**
     * Sets the value of the winBranch property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMissionNodeRef }
     *     
     */
    public void setWinBranch(XMLMissionNodeRef value) {
        this.winBranch = value;
    }

    /**
     * Gets the value of the loseBranch property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMissionNodeRef }
     *     
     */
    public XMLMissionNodeRef getLoseBranch() {
        return loseBranch;
    }

    /**
     * Sets the value of the loseBranch property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMissionNodeRef }
     *     
     */
    public void setLoseBranch(XMLMissionNodeRef value) {
        this.loseBranch = value;
    }

    /**
     * Gets the value of the ending property.
     * 
     * @return
     *     possible object is
     *     {@link XMLEndingRef }
     *     
     */
    public XMLEndingRef getEnding() {
        return ending;
    }

    /**
     * Sets the value of the ending property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLEndingRef }
     *     
     */
    public void setEnding(XMLEndingRef value) {
        this.ending = value;
    }

}
