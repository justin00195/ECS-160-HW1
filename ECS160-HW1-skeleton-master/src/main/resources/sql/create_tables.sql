CREATE DATABASE socialmedia_db;

CREATE TABLE Posts(
    post_id SERIAL PRIMARY KEY,
    post_contents TEXT NOT NULL,
    number_words INT,
    number_replies INT,
    weight DOUBLE PRECISION,
    parent_id INT REFERENCES Posts(post_id)
);