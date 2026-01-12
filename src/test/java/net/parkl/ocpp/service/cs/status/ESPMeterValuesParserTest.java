package net.parkl.ocpp.service.cs.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.idsg.steve.ocpp.ws.JsonObjectMapper;
import lombok.SneakyThrows;
import net.parkl.ocpp.entities.TransactionStart;
import net.parkl.ocpp.module.esp.model.ESPMeterValues;
import net.parkl.ocpp.service.driver.DriverTestBase;
import ocpp.cs._2015._10.MeterValuesRequest;
import ocpp.cs._2015._10.SampledValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ESPMeterValuesParserTest extends DriverTestBase {
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ActivePowerParser activePowerParser;

    @Mock
    private StateOfChargeParser stateOfChargeParser;

    @Mock
    private TotalEnergyParser totalEnergyParser;

    @InjectMocks
    private ESPMeterValuesParser espMeterValuesParser;

    @BeforeEach
    void setUp() {
        when(applicationContext.getBean(ActivePowerParser.class)).thenReturn(activePowerParser);
        when(applicationContext.getBean(StateOfChargeParser.class)).thenReturn(stateOfChargeParser);
        when(applicationContext.getBean(TotalEnergyParser.class)).thenReturn(totalEnergyParser);
    }

    @Test
    @SneakyThrows
    void testNullMeasurandMeterValues() {
        InputStream is = getClass().getResourceAsStream("/metervalues.json");
        assertNotNull(is);

        String json = new String(is.readAllBytes());

        ObjectMapper objectMapper = JsonObjectMapper.INSTANCE.getMapper();
        MeterValuesRequest request = objectMapper.readValue(json, MeterValuesRequest.class);
        assertNotNull(request);

        TransactionStart transactionStart = new TransactionStart();
        ESPMeterValues espMeterValues = espMeterValuesParser.parseMeterValues(transactionStart, request.getMeterValue());
        assertNotNull(espMeterValues);

        // Verify that each parser was called with the correct arguments
        verify(activePowerParser, times(request.getMeterValue().size() * request.getMeterValue().get(0).getSampledValue().size()))
                .parseMeterValue(any(ESPMeterValues.class), eq(transactionStart), any(SampledValue.class));
        // Add similar verifications for other parsers
    }
}