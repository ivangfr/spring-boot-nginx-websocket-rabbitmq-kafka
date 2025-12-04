package com.ivanfranchin.newsapp.news.event;

import com.ivanfranchin.newsapp.news.model.News;

public record NewsEvent(String id, String description) {
    public static NewsEvent from(News news) {
        return new NewsEvent(news.getId(), news.getDescription());
    }
}
