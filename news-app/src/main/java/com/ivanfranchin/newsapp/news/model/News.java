package com.ivanfranchin.newsapp.news.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@Entity
public class News {

    @Id
    @Column(nullable = false)
    private String id;
    @Column(nullable = false, length = 5000)
    private String description;
    @Min(0)
    private int likes;
    @Min(0)
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
