package com.ivanfranchin.newsapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.ivanfranchin.newsapp.news.dto.NewsResponse;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NewsAppE2ERestApiIT extends AbstractTestcontainers {

  private static final String API_PATH = "/api/news";

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  void publishNews_thenRetrievedViaApi() {
    NewsResponse createdNews = postNews("Breaking news from integration test");

    assertThat(createdNews).isNotNull();
    assertThat(createdNews.id()).isNotNull();
    assertThat(createdNews.description()).isEqualTo("Breaking news from integration test");
    assertThat(createdNews.likes()).isEqualTo(0);
    assertThat(createdNews.dislikes()).isEqualTo(0);

    ResponseEntity<List> response = testRestTemplate.getForEntity(API_PATH, List.class);
    @SuppressWarnings("unchecked")
    List<NewsResponse> allNews = response.getBody();
    assertThat(allNews).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void publishMultipleNews_andRetrieveAll() {
    for (int i = 0; i < 3; i++) {
      NewsResponse created = postNews("News item " + i);
      assertThat(created).isNotNull();
      assertThat(created.description()).contains("News item " + i);
    }

    ResponseEntity<List> response = testRestTemplate.getForEntity(API_PATH, List.class);
    @SuppressWarnings("unchecked")
    List<NewsResponse> allNews = response.getBody();
    assertThat(allNews).hasSizeGreaterThanOrEqualTo(3);
  }

  private NewsResponse postNews(String description) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String requestBody = "{\"description\":\"" + description + "\"}";

    return testRestTemplate.postForObject(
        API_PATH, new HttpEntity<>(requestBody, headers), NewsResponse.class);
  }
}
