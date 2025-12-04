package com.ivanfranchin.newsapp.reaction;

import com.ivanfranchin.newsapp.news.NewsService;
import com.ivanfranchin.newsapp.news.model.News;
import com.ivanfranchin.newsapp.reaction.event.ReactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReactionEventConsumer {

    private final NewsService newsService;

    @Bean
    Consumer<ReactionEvent> reactions() {
        return reactionEvent -> {
            log.info("Received ReactionEvent from Apache Kafka: {}", reactionEvent);
            News news = newsService.validateAndGetNews(reactionEvent.newsId());
            ReactionEvent.Reaction reaction = reactionEvent.reaction();
            switch (reaction) {
                case LIKE -> news.incrementLikes();
                case DISLIKE -> news.incrementDislikes();
                default -> log.warn("Unknown reaction: {}", reaction);
            }
            newsService.saveNews(news);
        };
    }
}
