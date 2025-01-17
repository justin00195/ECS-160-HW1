package com.SQLDatabaseManager;

import java.sql.*;
import java.util.*;

/**
 * This class is used to connect to the socialmedia_db postgresql database
 */
public class SQLDatabaseManager {
    private final String url;
    private final String username;
    private final String password;

    // Constructor
    public SQLDatabaseManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // method to insert a post into the Posts table
    public void InsertPost(Post post) throws SQLException {
        String insert_cmd = "INSERT INTO Posts (post_contents, number_words, number_replies, weight, parent_id) VALUES (?, ?, ?, ?, ?)";

        // make sure our connection and insert_cmd string are valid
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement command = connection.prepareStatement(insert_cmd)) {
            // insert values to the insert_cmd
            command.setString(1, post.post_content);
            command.setInt(2, post.number_words);
            command.setInt(3, post.number_replies);

            if (post.weight != null) {
                command.setDouble(4, post.weight);
            } else {
                command.setNull(4, Types.NULL);
            }

            if (post.parent_id != null) {
                command.setDouble(5, post.parent_id);
            } else {
                command.setNull(5, Types.NULL);
            }

            // run the insert command
            command.executeUpdate();
        } catch (SQLException error) {
            error.printStackTrace();
        }
    }

    public List<Post> getPosts() throws SQLException {
        String select_cmd = "SELECT * FROM Posts";
        List<Post> posts = new ArrayList<Post>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement command = connection.createStatement();
             ResultSet resultSet = command.executeQuery(select_cmd)) {
            // iterate through Posts and extract them as a Post
            while(resultSet.next()) {
                posts.add(new Post(resultSet.getInt("post_id"),
                                   resultSet.getString("post_contents"),
                                   resultSet.getInt("number_words"),
                                   resultSet.getInt("number_replies"),
                                   resultSet.getDouble("weight"),
                                   resultSet.getInt("parent_id")
                                  ));
            }
        } catch (SQLException error) {
            error.printStackTrace();
        }

        return posts;
    }
}
