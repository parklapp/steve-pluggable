package net.parkl.ocpp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ocpp_transaction_stop")
@Getter
@Setter
public class TransactionStop {
    @EmbeddedId
    private TransactionStopId transactionStopId;

    @MapsId("transactionPk")
    @JoinColumn(name = "transaction_pk", referencedColumnName = "transaction_pk",
            foreignKey = @ForeignKey(name = "FK_transaction_stop_transaction_pk"))
    @ManyToOne(cascade = CascadeType.REMOVE)
    private TransactionStart transaction;

    @Column(name = "event_actor", length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStopEventActor eventActor;

    @Column(name = "stop_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date stopTimestamp;

    @Column(name = "stop_value")
    private String stopValue;

    @Column(name = "stop_reason", length = 255, nullable = true)
    private String stopReason;
}