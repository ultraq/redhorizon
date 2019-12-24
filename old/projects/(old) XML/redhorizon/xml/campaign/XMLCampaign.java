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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import redhorizon.cnc.CNCGameTypes;


/**
 * <p>Java class for Campaign complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Campaign">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.ultraq.net.nz/redhorizon/campaign}Name"/>
 *         &lt;element name="Description" type="{http://www.ultraq.net.nz/redhorizon/campaign}Description" minOccurs="0"/>
 *         &lt;element name="Mission" type="{http://www.ultraq.net.nz/redhorizon/campaign}Mission" maxOccurs="unbounded"/>
 *         &lt;element name="Ending" type="{http://www.ultraq.net.nz/redhorizon/campaign}Ending" maxOccurs="unbounded"/>
 *         &lt;element name="MissionTree" type="{http://www.ultraq.net.nz/redhorizon/campaign}MissionTree"/>
 *       &lt;/sequence>
 *       &lt;attribute name="GameType" type="{http://www.ultraq.net.nz/redhorizon/common}GameTypes" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Campaign", propOrder = {
    "name",
    "description",
    "mission",
    "ending",
    "missionTree"
})
public class XMLCampaign {

    @XmlElement(name = "Name", required = true)
    protected XMLName name;
    @XmlElement(name = "Description")
    protected XMLDescription description;
    @XmlElement(name = "Mission", required = true)
    protected List<XMLMission> mission;
    @XmlElement(name = "Ending", required = true)
    protected List<XMLEnding> ending;
    @XmlElement(name = "MissionTree", required = true)
    protected XMLMissionTree missionTree;
    @XmlAttribute(name = "GameType")
    protected CNCGameTypes gameType;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link XMLName }
     *     
     */
    public XMLName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLName }
     *     
     */
    public void setName(XMLName value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link XMLDescription }
     *     
     */
    public XMLDescription getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLDescription }
     *     
     */
    public void setDescription(XMLDescription value) {
        this.description = value;
    }

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
     * {@link XMLMission }
     * 
     * 
     */
    public List<XMLMission> getMission() {
        if (mission == null) {
            mission = new ArrayList<XMLMission>();
        }
        return this.mission;
    }

    /**
     * Gets the value of the ending property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ending property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnding().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLEnding }
     * 
     * 
     */
    public List<XMLEnding> getEnding() {
        if (ending == null) {
            ending = new ArrayList<XMLEnding>();
        }
        return this.ending;
    }

    /**
     * Gets the value of the missionTree property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMissionTree }
     *     
     */
    public XMLMissionTree getMissionTree() {
        return missionTree;
    }

    /**
     * Sets the value of the missionTree property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMissionTree }
     *     
     */
    public void setMissionTree(XMLMissionTree value) {
        this.missionTree = value;
    }

    /**
     * Gets the value of the gameType property.
     * 
     * @return
     *     possible object is
     *     {@link CNCGameTypes }
     *     
     */
    public CNCGameTypes getGameType() {
        return gameType;
    }

    /**
     * Sets the value of the gameType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CNCGameTypes }
     *     
     */
    public void setGameType(CNCGameTypes value) {
        this.gameType = value;
    }

}