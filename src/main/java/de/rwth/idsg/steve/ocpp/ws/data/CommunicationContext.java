/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2024 SteVe Community Team
 * All Rights Reserved.
 *
 * Parkl Digital Technologies
 * Copyright (C) 2020-2021
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.ocpp.ws.data;

import de.rwth.idsg.ocpp.jaxb.ResponseType;
import de.rwth.idsg.steve.ocpp.CommunicationTask;
import de.rwth.idsg.steve.ocpp.ws.cluster.ClusterCommunicationMode;
import de.rwth.idsg.steve.ocpp.ws.cluster.ClusteredInvokerClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import jakarta.xml.ws.Response;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Default holder/context of incoming and outgoing messages.
 *
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 23.03.2015
 */
@Getter
@RequiredArgsConstructor
public class CommunicationContext {

    private final WebSocketSession session;
    private final ClusteredInvokerClient clusteredInvokerClient;
    private final String chargeBoxId;
    private final ClusterCommunicationMode clusterCommunicationMode;


    @Setter private String incomingString;
    @Setter private String outgoingString;

    @Setter private OcppJsonMessage incomingMessage;
    @Setter private OcppJsonMessage outgoingMessage;

    @Setter
    @Getter
    private String remoteMessageId;

    @Setter private FutureResponseContext futureResponseContext;

    // for incoming responses to previously sent requests
    private Consumer<OcppJsonResult> resultHandler;
    private Consumer<OcppJsonError> errorHandler;

    public boolean isSetOutgoingError() {
        return (outgoingMessage != null) && (outgoingMessage instanceof OcppJsonError);
    }

    @SuppressWarnings("unchecked")
    public void createResultHandler(CommunicationTask task) {
        // TODO: not so sure about this
        resultHandler = result -> task.getHandler(chargeBoxId)
                .handleResponse(new DummyResponse(result.getPayload()));

    }

    public void createRemoteResultHandler(String originPodIp) {
        resultHandler = result -> {
            //task.getHandler(chargeBoxId, true)
            //        .handleResponse(new DummyResponse(result.getPayload()));
            clusteredInvokerClient.callback(chargeBoxId, incomingString, originPodIp);
        };
    }

    public void createErrorHandler(CommunicationTask task) {
        // TODO: not so sure about this
        errorHandler = result -> task.defaultCallback()
            .success(chargeBoxId, result);

    }

    public void createRemoteErrorHandler(String originPodIp) {
        resultHandler = result -> {
            //task.addNewError(chargeBoxId, result.toString());
            clusteredInvokerClient.callback(chargeBoxId, incomingString, originPodIp);
        };
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DummyResponse implements Response<ResponseType> {
        private final ResponseType payload;

        @Override
        public Map<String, Object> getContext() {
            return null;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public ResponseType get() {
            return payload;
        }

        @Override
        public ResponseType get(long timeout, TimeUnit unit) {
            return payload;
        }
    }
}
