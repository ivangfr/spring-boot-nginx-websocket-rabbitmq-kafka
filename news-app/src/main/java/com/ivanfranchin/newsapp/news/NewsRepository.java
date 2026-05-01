package com.ivanfranchin.newsapp.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ivanfranchin.newsapp.news.model.News;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {}
