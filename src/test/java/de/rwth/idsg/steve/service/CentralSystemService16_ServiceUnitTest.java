package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.repository.dto.UpdateTransactionParams;
import de.rwth.idsg.steve.service.notification.OcppTransactionEnded;
import net.parkl.ocpp.entities.TransactionStart;
import net.parkl.ocpp.service.cs.ConnectorMeterValueService;
import net.parkl.ocpp.service.cs.TransactionService;
import ocpp.cs._2015._10.StopTransactionRequest;
import ocpp.cs._2015._10.StopTransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class CentralSystemService16_ServiceUnitTest {

    @InjectMocks
    private CentralSystemService16_Service service;
    @Mock
    private OcppTagService ocppTagService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ConnectorMeterValueService connectorMeterValueService;
    @Mock
    private NotificationService notificationService;

    @Test
    public void stopTransactionWithZeroTransactionIdWillRespondEmptyResponse() {
        StopTransactionRequest parameters = new StopTransactionRequest();
        parameters.setTransactionId(0);
        parameters.setIdTag("idTag");

        StopTransactionResponse response = service.stopTransaction(parameters, "testChargeBox");

        verify(transactionService, never()).updateTransaction(any(UpdateTransactionParams.class));
        verify(connectorMeterValueService, never()).insertMeterValues(anyList(), any(TransactionStart.class));
        verify(notificationService, never()).ocppTransactionEnded(any(OcppTransactionEnded.class));

        assertThat(response).isNotNull();
    }
}