import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

public class TestShowingDemoPlugin {
    @Test
    void test() {
        HttpClientResponse httpClientResponse = HttpClient.create().get().uri("http://localhost:3000").response().blockOptional().orElseThrow();

        Assertions.assertEquals(200,
                httpClientResponse.status().code(),
                "Could not get an OK response from the server provided by demo.plugin");
    }
}
