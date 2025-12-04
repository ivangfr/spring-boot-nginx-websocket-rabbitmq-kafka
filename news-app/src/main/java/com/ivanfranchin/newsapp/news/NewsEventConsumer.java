package com.ivanfranchin.newsapp.news;

import com.ivanfranchin.newsapp.news.event.NewsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewsEventConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Bean
    Consumer<NewsEvent> news() {
        return newsEvent -> {
            log.info("Received NewsEvent from RabbitMQ. Broadcasting it. {}", newsEvent);
            simpMessagingTemplate.convertAndSend("/topic/news", newsEvent);
        };
    }
}
