package com.rick.supertrading.worker.bot;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Playwright + headless Chromium implementation of the bot flow (spec §8.1):
 * navigate → fill credentials → submit → browse → click GREEN/RED → wait → logout.
 * On any failure a screenshot is captured and attached to the thrown exception.
 */
@Component
public class PlaywrightBotRunner implements BotRunner {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBotRunner.class);

    @Override
    public BotResult runSession(BotSessionRequest request) throws BotExecutionException {
        Map<String, String> selectors = request.selectors();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            try {
                page.navigate(request.loginUrl());
                page.fill(selectors.get("username"), request.username());
                page.fill(selectors.get("password"), request.password());
                page.press(selectors.get("password"), "Enter");

                // Browse the site for the configured period before acting.
                page.waitForTimeout(request.navigateMillis());

                String choiceSelector = switch (request.choice()) {
                    case GREEN -> selectors.get("green");
                    case RED -> selectors.get("red");
                };

                long startNanos = System.nanoTime();
                page.click(choiceSelector);
                int clickDurationMs = (int) ((System.nanoTime() - startNanos) / 1_000_000);
                String pageUrl = page.url();

                page.waitForTimeout(request.waitBeforeLogoutMs());
                page.click(selectors.get("logout"));

                log.info("Bot clicked {} at {} ({} ms)", request.choice(), pageUrl, clickDurationMs);
                return new BotResult(pageUrl, clickDurationMs);
            } catch (Exception ex) {
                throw new BotExecutionException(
                        "Bot session failed: " + ex.getMessage(), ex, safeScreenshot(page));
            } finally {
                browser.close();
            }
        }
    }

    private static byte[] safeScreenshot(Page page) {
        try {
            return page.screenshot();
        } catch (Exception ignored) {
            return null;
        }
    }
}
