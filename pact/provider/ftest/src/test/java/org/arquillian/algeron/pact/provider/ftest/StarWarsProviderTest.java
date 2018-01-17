package org.arquillian.algeron.pact.provider.ftest;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import java.io.File;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.arquillian.algeron.pact.provider.spi.Target;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.pact.provider.spi.VerificationReports;
import org.arquillian.algeron.provider.core.retriever.ContractsFolder;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Provider("planets_provider")
@ContractsFolder("pacts")
@VerificationReports(value = {"recorder"})
public class StarWarsProviderTest {

    @ClassRule
    public static final ClientDriverRule embeddedService = new ClientDriverRule(8332);

    @ArquillianResource
    Target target;

    @ArquillianResource
    org.arquillian.algeron.pact.provider.core.ContractExposer contractExposer;

    @BeforeClass
    public static void recordServerInteractions() {
        embeddedService.addExpectation(
            onRequestTo("/rest/planet/orbital/average")
                .withMethod(ClientDriverRequest.Method.GET),
            giveResponse("1298.3", "text/plain").withStatus(200));

        embeddedService.addExpectation(
            onRequestTo("/rest/planet/orbital/biggest")
                .withMethod(ClientDriverRequest.Method.GET),
            giveResponseAsBytes(StarWarsProviderTest.class.getResourceAsStream("/server.json"),
                "application/json").withStatus(200));
    }

    @Test
    public void validateProvider() {
        target.testInteraction();
    }

    @Test
    public void should_be_able_to_connect_exposed_contracts_within_provider_test() throws IOException {
        try {
            contractExposer.start(new File("src/test/resources/pacts/planets_consumer-planets_provider.json"));

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(contractExposer.getUrl() + "/rest/planet/orbital/average")
                .build();

            Response response = client.newCall(request).execute();
            assertThat(response.body().string()).isEqualTo("1298.3");

        } finally {
            contractExposer.stop();
        }
    }

}
