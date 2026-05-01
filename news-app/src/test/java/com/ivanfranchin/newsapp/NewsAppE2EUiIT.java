package com.ivanfranchin.newsapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import com.ivanfranchin.newsapp.news.NewsRepository;
import com.ivanfranchin.newsapp.news.dto.NewsResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NewsAppE2EUiIT extends AbstractTestcontainers {

  @Autowired private TestRestTemplate testRestTemplate;

  @Autowired private NewsRepository newsRepository;

  private static Playwright playwright;
  private static Browser browser;
  private static BrowserContext context;
  private static String baseUrl;

  @BeforeAll
  static void setUpPlaywright(@LocalServerPort int port) {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    context = browser.newContext();
    baseUrl = "http://localhost:" + port;
  }

  @Test
  void createNews_viaApi_verifyRealTimeUpdate_inUi() {
    Page page = context.newPage();
    List<String> consoleMessages = new CopyOnWriteArrayList<>();
    page.onConsoleMessage(msg -> consoleMessages.add(msg.type() + ": " + msg.text()));

    page.navigate(baseUrl);
    page.waitForLoadState();

    NewsResponse createdNews = createNewsViaApi("E2E real-time test");

    var newsEntity = newsRepository.findById(createdNews.id());
    assertThat(newsEntity).isPresent();

    String newsSelector = newsSelector(createdNews.id());
    awaitVisible(page, newsSelector);

    assertThat(page.locator(newsSelector).isVisible()).isTrue();
    assertThat(page.locator(newsSelector + " .news-description").textContent())
        .isEqualTo("E2E real-time test");
    assertThat(page.locator(newsSelector).getByText("Like").first().isVisible()).isTrue();
    assertThat(page.locator(newsSelector).getByText("Dislike").first().isVisible()).isTrue();

    assertNoErrors(consoleMessages);

    page.close();
  }

  @Test
  void clickLikeButton_verifyReactionSent_viaDatabase() {
    Page page = context.newPage();
    List<String> consoleMessages = new CopyOnWriteArrayList<>();
    page.onConsoleMessage(msg -> consoleMessages.add(msg.type() + ": " + msg.text()));

    page.navigate(baseUrl);
    page.waitForLoadState();

    NewsResponse createdNews = createNewsViaApi("E2E like test");

    String newsSelector = newsSelector(createdNews.id());
    awaitVisible(page, newsSelector);

    sendReaction(page, createdNews.id(), "LIKE");

    awaitReactionCount(createdNews, 1, 0);

    var newsEntity = newsRepository.findById(createdNews.id());
    assertThat(newsEntity).isPresent();
    assertThat(newsEntity.get().getLikes()).isGreaterThanOrEqualTo(1);

    page.close();
  }

  @Test
  void clickDislikeButton_verifyReactionSent_viaDatabase() {
    Page page = context.newPage();
    List<String> consoleMessages = new CopyOnWriteArrayList<>();
    page.onConsoleMessage(msg -> consoleMessages.add(msg.type() + ": " + msg.text()));

    page.navigate(baseUrl);
    page.waitForLoadState();

    NewsResponse createdNews = createNewsViaApi("E2E dislike test");

    String newsSelector = newsSelector(createdNews.id());
    awaitVisible(page, newsSelector);

    sendReaction(page, createdNews.id(), "DISLIKE");

    awaitReactionCount(createdNews, 0, 1);

    var newsEntity = newsRepository.findById(createdNews.id());
    assertThat(newsEntity).isPresent();
    assertThat(newsEntity.get().getDislikes()).isGreaterThanOrEqualTo(1);

    page.close();
  }

  @Test
  void publishMultipleNews_verifyAllAppearInUi() {
    Page page = context.newPage();
    List<String> consoleMessages = new CopyOnWriteArrayList<>();
    page.onConsoleMessage(msg -> consoleMessages.add(msg.type() + ": " + msg.text()));

    page.navigate(baseUrl);
    page.waitForLoadState();

    List<NewsResponse> createdNewsList = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      createdNewsList.add(createNewsViaApi("E2E multiple news item " + i));
    }

    for (NewsResponse news : createdNewsList) {
      String newsSelector = newsSelector(news.id());
      awaitVisible(page, newsSelector);
      assertThat(page.locator(newsSelector).isVisible()).isTrue();
      assertThat(page.locator(newsSelector).getByText("Like").first().isVisible()).isTrue();
      assertThat(page.locator(newsSelector).getByText("Dislike").first().isVisible()).isTrue();
    }

    assertNoErrors(consoleMessages);

    page.close();
  }

  private NewsResponse createNewsViaApi(String description) {
    String requestBody = "{\"description\":\"" + description + "\"}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return testRestTemplate.postForObject(
        "/api/news", new HttpEntity<>(requestBody, headers), NewsResponse.class);
  }

  private String newsSelector(String newsId) {
    return ".news-item[data-news-id='" + newsId + "']";
  }

  private void awaitVisible(Page page, String selector) {
    Awaitility.await()
        .atMost(90, TimeUnit.SECONDS)
        .pollInterval(2, TimeUnit.SECONDS)
        .until(
            () -> {
              try {
                return page.locator(selector).isVisible();
              } catch (Exception e) {
                return false;
              }
            });
  }

  private void assertNoErrors(List<String> consoleMessages) {
    List<String> errors =
        consoleMessages.stream()
            .map(m -> m.toLowerCase())
            .filter(m -> m.startsWith("error:"))
            .toList();
    assertThat(errors).isEmpty();
  }

  private void sendReaction(Page page, String newsId, String reaction) {
    String jsCode =
        "() => {"
            + "if (!stompClient || !stompClient.connected) {"
            + "  console.warn('Not connected');"
            + "  return false;"
            + "}"
            + "const payload = { id: Date.now(), newsId: '"
            + newsId
            + "', reaction: '"
            + reaction
            + "' };"
            + "stompClient.send('/app/reaction', {}, JSON.stringify(payload));"
            + "console.log('Reaction sent');"
            + "return true;"
            + "}";
    page.evaluate(jsCode);
  }

  private void awaitReactionCount(
      NewsResponse createdNews, int expectedLikes, int expectedDislikes) {
    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                newsRepository
                    .findById(createdNews.id())
                    .map(n -> n.getLikes() >= expectedLikes && n.getDislikes() >= expectedDislikes)
                    .orElse(false));
  }

  @AfterAll
  static void tearDownPlaywright() {
    if (context != null) context.close();
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }
}
