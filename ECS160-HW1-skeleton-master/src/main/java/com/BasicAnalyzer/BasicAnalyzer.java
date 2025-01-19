package com.BasicAnalyzer;

import com.SQLDatabaseManager.*;
import com.google.gson.*;

import java.net.*;
import java.util.*;
import java.io.*;

public class BasicAnalyzer {
    // NOTE: Replace these with the link to the database on your machine and your own postgreSQL username and password
    private static final String sql_url = "";
    private static final String sql_username = "postgre";
    private static final String sql_password = "tron_quay_323!";

    private static SQLDatabaseManager database_manager = new SQLDatabaseManager(sql_url, sql_username, sql_password);

    public static List<Post> posts = new ArrayList<Post>();

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        String json_path = "input.json";
        boolean weighted = false;

        // parse our args
        for (String arg : args) {
            String cur_arg = arg.replaceAll("\\s", "");
            if (cur_arg.equals("weighted=true")) {
                weighted = true;
            } else if (cur_arg.equals("weighted=false")) {
                weighted = false;
            } else {
                json_path = arg;
            }
        }

        // parse the json file
        extractJsonPosts(json_path);

        // if we have weighted=true passed through the command line, calculate the weights of our posts
        if (weighted) {
            calculate_weight();
        }
    }

    public static void extractJsonPosts(String json_path) throws URISyntaxException, FileNotFoundException {
        URL resource = JsonDeserializer.class.getClassLoader().getResource(json_path);

        JsonParser parser = new JsonParser();

        JsonElement element = parser.parse(new FileReader(resource.toURI().getPath()));

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();
            for (JsonElement feedObject: feedArray) {
                // Check if you have the thread key
                if (feedObject.getAsJsonObject().has("thread")) {
                    // parse the post
                    JsonObject thread = feedObject.getAsJsonObject().getAsJsonObject("thread");
                    parseThread(thread);
                }
            }
        }
    }

    // method to parse the thread from our json file
    public static void parseThread(JsonObject thread) {
        String post_id = null;
        String post_content = null;
        int number_words = 0;
        int number_replies = 0;
        String parent_id = null;

        // if we have a post, extract the data from it
        if (thread.has("post")) {
            JsonObject json_post = thread.getAsJsonObject("post");

            post_id = json_post.getAsJsonPrimitive("cid").getAsString();
            number_replies = json_post.getAsJsonPrimitive("replyCount").getAsInt();

            // the text is contained within the record in a post
            if (json_post.has("record")) {
                post_content = json_post.getAsJsonObject("record").getAsJsonPrimitive("text").getAsString();
                number_words = post_content.split("\s+").length; // get the number of words
            }

            // if the post is a reply, get the parent id
            if (json_post.has("reply")) {
                parent_id = json_post.getAsJsonObject("reply")
                                     .getAsJsonObject("parent")
                                     .getAsJsonPrimitive("cid")
                                     .getAsString();
            }
        }

        // append the post to our posts list
        posts.add(new Post(post_id, post_content, number_words, number_replies, 0, parent_id));

        // if our thread has any replies, recursively go through the replies with parseThread
        if (thread.has("replies")) {
            JsonArray replies = thread.getAsJsonArray("replies");

            for (JsonElement reply : replies) {
                JsonObject reply_thread = reply.getAsJsonObject();
                parseThread(reply_thread);
            }
        }
    }

    // calculates the weight for all Posts in posts
    public static void calculate_weight() {
        // get the post with most amount of words
        Post largest_post = posts.stream()
                                 .max(Comparator.comparingInt(post -> post.number_words))
                                 .orElse(null);

        // iterate through each post and update their weights
        for (int i = 0; i < posts.size(); ++i) {
            Post post = posts.get(i);
            double weight = (1 + ((double) post.number_words / largest_post.number_words));
            posts.set(i, post);
        }
    }
}
