package com.ivanfranchin.newsapp.news;

import com.ivanfranchin.newsapp.news.dto.CreateNewsRequest;
import com.ivanfranchin.newsapp.news.dto.NewsResponse;
import com.ivanfranchin.newsapp.news.event.NewsEvent;
import com.ivanfranchin.newsapp.news.model.News;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/news")
public class NewsRestController {

    private final NewsService newsService;
    private final StreamBridge streamBridge;

    @GetMapping("/{id}")
    public NewsResponse getNews(@PathVariable String id) {
        return NewsResponse.from(newsService.validateAndGetNews(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public NewsResponse publishNews(@Valid @RequestBody(required = false) CreateNewsRequest createNewsRequest) {
        String description = createNewsRequest == null || createNewsRequest.description().isBlank() ?
                generateRandomDescription() : createNewsRequest.description();
        News news = newsService.saveNews(new News(UUID.randomUUID().toString(), description));
        NewsEvent newsEvent = NewsEvent.from(news);
        log.info("Publishing NewsEvent. {}", newsEvent);
        streamBridge.send("news-out-0", CloudEventMessageBuilder.withData(newsEvent).build());
        return NewsResponse.from(news);
    }

    private String generateRandomDescription() {
        int idx = ThreadLocalRandom.current().nextInt(SAMPLE_HEADLINES.size());
        return SAMPLE_HEADLINES.get(idx);
    }

    private static final List<String> SAMPLE_HEADLINES = List.of(
            "Local startup secures unexpected funding boost",
            "Scientists discover promising new battery material",
            "Community garden transforms vacant lot into oasis",
            "Major breakthrough in AI-assisted healthcare announced",
            "City approves ambitious new public transport plan",
            "Artists collaborate on large-scale public mural project",
            "Innovative recycling program reduces neighborhood waste",
            "Historic building repurposed into affordable housing",
            "New cafe becomes hub for remote workers",
            "Researchers publish study on urban biodiversity gains"
    );
}