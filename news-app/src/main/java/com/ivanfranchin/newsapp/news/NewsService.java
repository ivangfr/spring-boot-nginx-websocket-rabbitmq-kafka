package com.ivanfranchin.newsapp.news;

import com.ivanfranchin.newsapp.news.exception.NewsNotFoundException;
import com.ivanfranchin.newsapp.news.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public News saveNews(News news) {
        return newsRepository.save(news);
    }

    public News validateAndGetNews(String newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News not found: " + newsId));
    }
}
