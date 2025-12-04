package com.ivanfranchin.newsapp.news;

import com.ivanfranchin.newsapp.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {
}
