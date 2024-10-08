
package ocpp.cs._2010._08;

import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import de.rwth.idsg.ocpp.jaxb.RequestType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.ToString;
import org.joda.time.DateTime;


/**
 * Defines the StartTransaction.req PDU
 * 
 * <p>Java class for StartTransactionRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StartTransactionRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="connectorId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="idTag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="meterStart" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartTransactionRequest", propOrder = {
    "connectorId",
    "idTag",
    "timestamp",
    "meterStart"
})
@ToString
public class StartTransactionRequest
    implements RequestType
{

    protected int connectorId;
    @XmlElement(required = true)
    protected String idTag;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime timestamp;
    protected int meterStart;

    /**
     * Gets the value of the connectorId property.
     * 
     */
    public int getConnectorId() {
        return connectorId;
    }

    /**
     * Sets the value of the connectorId property.
     * 
     */
    public void setConnectorId(int value) {
        this.connectorId = value;
    }

    public boolean isSetConnectorId() {
        return true;
    }

    /**
     * Gets the value of the idTag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdTag() {
        return idTag;
    }

    /**
     * Sets the value of the idTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdTag(String value) {
        this.idTag = value;
    }

    public boolean isSetIdTag() {
        return (this.idTag!= null);
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestamp(DateTime value) {
        this.timestamp = value;
    }

    public boolean isSetTimestamp() {
        return (this.timestamp!= null);
    }

    /**
     * Gets the value of the meterStart property.
     * 
     */
    public int getMeterStart() {
        return meterStart;
    }

    /**
     * Sets the value of the meterStart property.
     * 
     */
    public void setMeterStart(int value) {
        this.meterStart = value;
    }

    public boolean isSetMeterStart() {
        return true;
    }

    /**
     * Sets the value of the connectorId property.
     * 
     * @param value
     *     allowed object is
     *     int
     * @return
     *     The class instance
     */
    public StartTransactionRequest withConnectorId(int value) {
        setConnectorId(value);
        return this;
    }

    /**
     * Sets the value of the idTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     * @return
     *     The class instance
     */
    public StartTransactionRequest withIdTag(String value) {
        setIdTag(value);
        return this;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateTime }
     * @return
     *     The class instance
     */
    public StartTransactionRequest withTimestamp(DateTime value) {
        setTimestamp(value);
        return this;
    }

    /**
     * Sets the value of the meterStart property.
     * 
     * @param value
     *     allowed object is
     *     int
     * @return
     *     The class instance
     */
    public StartTransactionRequest withMeterStart(int value) {
        setMeterStart(value);
        return this;
    }

}
