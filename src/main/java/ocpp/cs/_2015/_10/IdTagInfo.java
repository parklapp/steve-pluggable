
package ocpp.cs._2015._10;

import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.ToString;
import org.joda.time.DateTime;


/**
 * <p>Java class for IdTagInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IdTagInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="status" type="{urn://Ocpp/Cs/2015/10/}AuthorizationStatus"/&gt;
 *         &lt;element name="expiryDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="parentIdTag" type="{urn://Ocpp/Cs/2015/10/}IdToken" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdTagInfo", propOrder = {
    "status",
    "expiryDate",
    "parentIdTag"
})
@ToString
public class IdTagInfo {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected AuthorizationStatus status;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime expiryDate;
    protected String parentIdTag;

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link AuthorizationStatus }
     *     
     */
    public AuthorizationStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorizationStatus }
     *     
     */
    public void setStatus(AuthorizationStatus value) {
        this.status = value;
    }

    public boolean isSetStatus() {
        return (this.status!= null);
    }

    /**
     * Gets the value of the expiryDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the value of the expiryDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiryDate(DateTime value) {
        this.expiryDate = value;
    }

    public boolean isSetExpiryDate() {
        return (this.expiryDate!= null);
    }

    /**
     * Gets the value of the parentIdTag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentIdTag() {
        return parentIdTag;
    }

    /**
     * Sets the value of the parentIdTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentIdTag(String value) {
        this.parentIdTag = value;
    }

    public boolean isSetParentIdTag() {
        return (this.parentIdTag!= null);
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorizationStatus }
     * @return
     *     The class instance
     */
    public IdTagInfo withStatus(AuthorizationStatus value) {
        setStatus(value);
        return this;
    }

    /**
     * Sets the value of the expiryDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateTime }
     * @return
     *     The class instance
     */
    public IdTagInfo withExpiryDate(DateTime value) {
        setExpiryDate(value);
        return this;
    }

    /**
     * Sets the value of the parentIdTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     * @return
     *     The class instance
     */
    public IdTagInfo withParentIdTag(String value) {
        setParentIdTag(value);
        return this;
    }

}
