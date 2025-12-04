package com.ivanfranchin.newsapp.news.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@Entity
public class News {

    @Id
    private String id;
    private String description;
    private int likes;
    private int dislikes;

    public News(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void incrementDislikes() {
        this.dislikes++;
    }
}
