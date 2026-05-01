package com.ivanfranchin.newsapp.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNewsRequest(
    @NotBlank(message = "Description is required") @Size(min = 1, max = 5000, message = "Description must be between 1 and 5000 characters") String description) {}
