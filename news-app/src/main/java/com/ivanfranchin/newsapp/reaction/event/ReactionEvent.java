package com.ivanfranchin.newsapp.reaction.event;

public record ReactionEvent(String id, String newsId, Reaction reaction) {

    public enum Reaction {
        LIKE,
        DISLIKE
    }
}
