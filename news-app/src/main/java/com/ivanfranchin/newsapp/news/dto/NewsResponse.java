package com.ivanfranchin.newsapp.news.dto;

import com.ivanfranchin.newsapp.news.model.News;

public record NewsResponse(String id, String description, int likes, int dislikes) {

    public static NewsResponse from(News news) {
        return new NewsResponse(news.getId(), news.getDescription(), news.getLikes(), news.getDislikes());
    }
}
