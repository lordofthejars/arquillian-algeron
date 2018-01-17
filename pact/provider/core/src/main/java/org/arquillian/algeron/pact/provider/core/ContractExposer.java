package org.arquillian.algeron.pact.provider.core;

import au.com.dius.pact.consumer.MockHttpServer;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ContractExposer {

    private MockProviderConfig config;
    private  MockHttpServer mockHttpServer;

    public void start(File contractFile) {
        config = MockProviderConfig.createDefault();
        config.setPort(0);

        this.mockHttpServer = new MockHttpServer(loadPactfile(contractFile), config);
        mockHttpServer.start();
        waitForServer(mockHttpServer);

    }

    public void start(File contractFile, int port) {
        config = MockProviderConfig.createDefault();
        config.setPort(port);

        this.mockHttpServer = new MockHttpServer(loadPactfile(contractFile), config);
        mockHttpServer.start();
        waitForServer(mockHttpServer);

    }

    public String getUrl() {
        return mockHttpServer.getUrl();
    }

    public void stop() {
        // According to pact code we need to set this sleep to stabilize the server before shutdown.
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RequestResponsePact loadPactfile(File contractFile) {
        final Pact pact = PactReader.loadPact(contractFile);

        final List<RequestResponseInteraction> collect = pact.getInteractions()
            .stream()
            .map(i -> (RequestResponseInteraction) i)
            .collect(Collectors.toList());

        return new RequestResponsePact(pact.getProvider(), pact.getConsumer(), collect);
    }

    private void waitForServer(MockHttpServer mockHttpServer) {
        try {
            org.apache.http.client.fluent.Request.Options(mockHttpServer.getUrl())
                .addHeader("X-PACT-BOOTCHECK", "true")
                .execute();

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
