package com.SQLDatabaseManager;

/**
 * Represents a post from BlueSky
 */

public class Post {
    public int post_id;
    public String post_content;
    public int number_words;
    public int number_replies;
    public Double weight;
    public Integer parent_id;

    // Constructor
    public Post(int post_id,
                String post_content,
                int number_words,
                int number_replies,
                double weight,
                int parent_id) {
        this.post_id = post_id;
        this.post_content = post_content;
        this.number_words = number_words;
        this.number_replies = number_replies;
        this.weight = weight;
        this.parent_id = parent_id;
    }
}
