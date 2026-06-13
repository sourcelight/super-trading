package com.rick.supertrading.e2e;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.worker.bot.BotResult;
import com.rick.supertrading.worker.bot.BotSessionRequest;
import com.rick.supertrading.worker.bot.PlaywrightBotRunner;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real browser flow against a tiny mock target site (spec §12 step 8). Verifies the
 * Playwright login → click → logout path end to end.
 *
 * <p>Opt-in: runs only with {@code -De2e.playwright=true} and Playwright browsers
 * installed (CI step:
 * {@code mvn -pl worker exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=install}).
 */
@EnabledIfSystemProperty(named = "e2e.playwright", matches = "true")
class PlaywrightMockSiteIT {

    private static final String PAGE = """
            <!doctype html><html><body>
              <input id="u"/>
              <input id="p" type="password"/>
              <button class="g">Green</button>
              <button class="r">Red</button>
              <button id="o">Logout</button>
            </body></html>
            """;

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startMockSite() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] body = PAGE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopMockSite() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void drivesLoginClickAndLogout() throws Exception {
        var runner = new PlaywrightBotRunner();
        var request = new BotSessionRequest(
                baseUrl,
                Map.of("username", "#u", "password", "#p", "green", ".g", "red", ".r", "logout", "#o"),
                "bot-user",
                "secret",
                Choice.GREEN,
                0,
                0);

        BotResult result = runner.runSession(request);

        assertThat(result.pageUrl()).contains("127.0.0.1");
        assertThat(result.clickDurationMs()).isGreaterThanOrEqualTo(0);
    }
}
