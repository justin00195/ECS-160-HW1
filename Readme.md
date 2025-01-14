# ECS160-HW1 
## _(Due date: 1/27)_
## Problem 1: Basic analysis of social media posts 

_Learning objectives:_ 
1. Java basics: Encapsulation, Inheritance, File I/O, Exceptions.
2. Testing: JUnit, continuous integration via Github Actions.
3. Tools and libraries: Maven, adding dependencies to `pom.xml`, Gson for parsing JSON files, Apache's Common CLI for parsing command line interfaces, databases (NoSQL or SQL).

_Problem Statement:_

You are provided with an `input.json` file located [here](https://github.com/davsec-teaching/ECS160-HW1-skeleton/blob/master/src/main/resources/input.json) that consists of thousands of social media posts from [Bluesky](https://bsky.app). Every post can contain one of more replies. If a post does not have any reply we will call it a `standalone post`.

Your goal is to write a Java program that first caches the posts and their replies in the Json file to a database. You can use the [Redis](https://redis.io/) database for your application. When storing the posts and replies in the Redis database, you should make sure that it is possible to reconstruct the parent-child relationships of a post and its replies from the data stored. Alternatively, you can use a SQL database such as [PostgreSQL]() and use relationships defined by primary-key and foreign-keys to store the data.

Then, your application should compute the following basic statistics for the provided posts and replies. These statistics are---the total number of posts, the average number of replies per post, and average interval between comments (for posts which have comments). Depending on an option provided on the command line (`weighted = true|false`), you will either compute a simple average, or a weighted average that depends on the length of the post or comments for the first two statistics (total number of posts, average number of replies per post). Weighted average: The goal of the weighted average computation is to provide more weightage for longer posts. The formula for the weight of a post:

$Weight = (1 + (NumOfWordsInPost/NumOfWordsInLongestPost)) $

Therefore, to compute the average number of replies, we will first count $NumOfWordsInLongestPost$. Then, for
each reply to each post, we will compute the $Weight$ of that reply, using the above formula. Then, we will compute the
total number of posts as follows---

$WeightedTotalPosts = (\sum_{n=1}^N  (Weight_n) )  $

Here, $N$ is the total number of posts, and $Weight_n$ is the weight of the post $n$.

And, we will compute
the average number of replies, as follows---

$WeightedAvgNumReplies = (\sum_{n=1}^N (\sum_{m=1}^M (Weight_m) ) / N $

Here, $N$ is the total number of posts, $M$ is the total replies of post $n$. $Weight_m$ is the weight of the reply $m$. 

The full path of the `input.json` file will also optionally be provided on the command line. If no option is provided, then the `input.json` file provided in `src/resources/input.json` should be used.

**Getting started**
We will use the [IntelliJ IDE](https://www.jetbrains.com/help/idea/getting-started.html) for all our development, testing, and deployment tasks. Make sure to download and install the Community Edition. 

Once you have downloaded and installed IntelliJ IDE, clone the repository containing the skeleton code https://github.com/davsec-teaching/ECS160-HW1-skeleton, and open it as a project in the IntelliJ IDE.

We will use [Maven](https://maven.apache.org/) to manage all library dependencies. We need a library to parse JSON files, 
we will use Google's Gson for that, and a library to parse the command line options. We will use Apache Commons's CLI
library for that task.

If you choose to use Redis database, you can use the [Jedis](https://github.com/redis/jedis) library to interface between Java and Redis. If you are using a SQL database, Java presents a standard API called JDBC (Java Database Connectivity) to communicate with databases. Feel free to use the Postgres JDBC driver provided [here](https://github.com/pgjdbc/pgjdbc) to store (and load, in future assignments) the records from the PostgreSQL database.

**Adding library dependencies.**
Add the dependencies for Gson and commons-cli to `pom.xml`. Read the [Maven tutorial](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). Because we are using the IntelliJ IDE, if we click on the `Run` button, the Maven build steps will be automatically performed. The snippet from `pom.xml` to add the Gson and Apache Commons libraries are shown here. Please add the Jedis or Postgres JDBC dependency accordingly.

````
    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        <dependency>
            <groupId>commons-cli</groupId>
            <artifactId>commons-cli</artifactId>
            <version>1.5.0</version> <!-- Use the latest version -->
        </dependency>

    </dependencies>
````

**Class design**
Study the `input.json` file provided. JSON (Javascript Object Notation) is a popular data format for exchanging data between applications. Each JsonObject consists of multiple key-value pairs. You can find more information about [JSON](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/JSON) here. It will also be covered in the class.

Every social media post can either be a leaf post or consist of child posts, depending on whether or not the `thread` object contains a `replies` key or not. The structure of the JSON document is as follows:
````
{
    "feed": [
        {
            "thread": {
                "$type": "app.bsky.feed.defs#threadViewPost",
                "post": { ... // post contents },
                "replies" : {
                      "post": { .... // reply post contents },
                       ... // other stuff
                      }
                }
         },
         {
           "thread": { ... // thread contents as above }
         }
         ....
      ]
}               
````
The structure describes a nested structure for each thread, where each post can have multiple replies.
1. The `feed` key has a `JSonArray` consisting of multiple `JsonObjects` mapping to the key `thread`.
2. The JSONObject mapping to the `thread` key consists of many key-value pairs. The ones we are interested in are `post` and `replies`.
3. `post` maps to a JsonObject for the post.
4. `replies` maps to a JsonArray, where each `reply` JSonObject consists of a key-value pair for `post`.

Use the concepts of Encapsulation, Inheritance, and Recursion to model the structure described above. Note that you can model this in many different correct ways. To design the classes, you should ask yourself which key-value pairs from the Json you'll need to compute the desired statistics and create fields for these keys in the class. You may or may not choose to use Inheritance, but you should likely use a "Composite design pattern" to model the relationship where a `Thread` can contain many `Replies`.

**JSON parsing and Statistical Analysis**

We will use (Gson)[https://github.com/google/gson] to parse the `input.json` file. Here is some code to get you started.

```
        URL resource = JsonDeserializer.class.getClassLoader().getResource("input.json");

        JsonParser parser = new JsonParser();

        JsonElement element = parser.parse(new FileReader(resource.toURI().getPath()));

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();
            for (JsonElement feedObject: feedArray) {
                // Check if you have the thread key 
                if (feedObject.getAsJsonObject().has("thread")) {
                    // parse the post and any replies (recursively)? 
                } 
            }
        }
```

While parsing the Json file, load the Json objects into the Java classes for the _Composite_ pattern designed earlier.
Create a `Analyzer` interface class with two concrete sub-classes for the non-weighted and weighted calculations. Invoke the right analysis depending on the configuration option provided.

**Storing records to Redis / Postgres database**

**_1. Redis database_**
Design a class that handles the Redis communication. You can use the [Jedis](https://github.com/redis/jedis) library for the Redis communication.

This class will assign an auto-incrementing identifier for each post and reply, and have every post with replies store the identifiers of its replies. Refer to the `HSET`, 
`HGET,` and `HGETALL` [commands](https://redis.io/docs/latest/commands/) for how to store and fetch multi-field values. Check the `hset`, `hgetall`, and other related APIs for the [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) class here.

**_2. PostgreSQL database_**
If you choose to use a PostgreSQL database, first create a database called `socialmedia_db` and create one or more tables to store the posts and their replies. I will leave it up to you to decide if a single table is appropriate or two. However, ensure that each reply has a foreign key that refers to the primary key of the parent post record. 

Then, design a class that handles the database communication using the Postgres JDBC driver. Make sure to design class methods that handle storing and loading the records. 

**Testing**
We will write JUnit test cases for the Analysis classes designed in the previous step. Read more about JUnits [here](https://junit.org/junit5/docs/current/user-guide/).

To use JUnit testing framework, we first will have to add the JUnit jar library to `pom.xml` and run `mvn clean install` in IntelliJ terminal. We will use JUnit version 5.9.3 for our purposes.

````
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.3</version> 
    <scope>test</scope>
</dependency>
````

We will create a new class under the `tests/` directory. If your basic analyzer is called `BasicAnalyzer.java`, then the convention is that your test class should be called `BasicAnalyzerTest.java`. In that class add separate JUnit test methods to test each of the functions of the BasicAnalyzer.java class. Do the same for the weighted analysis class. Make sure to test all corner cases, such as empty posts, empty thread, _very_ long posts, and so on. We will run our own set of JUnit tests on the code, so it is in your benefit if you thoroughly test your code. Add all the required assertion checks.

We will set up a Continuous Integration pipeline where everytime we push, the Maven actions to build and test the application are carried out. Check out [this](https://docs.github.com/en/actions/writing-workflows/quickstart) for basics of Github actions and [Github Actions: Maven](https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven) for more Maven specific information.

We will add a badge indicating the status of our build to our project page. First, create a `README.md` at the top-level of the repository. Then, click on the `Actions` tab on the main project repository. Then, click on the name of the action on the left panel. In the right part of the screen, you'll see the ellipsis (...). Click on `Create status badge`. This will give you the Markdown text that you can add to the `README.md`.

**_Submission_**

Please create a **PRIVATE** Github code repo and commit code regularly to it. Your commit history will play a role in your grade. 
To make the final submission, commit your code to the Github repo and tag it. In a text file specify the following:
1. Link to your github and tag. This link should show the "tests passed" logo.
2. Command to clone your repository and apply the tag.
3. A tar-ball of your repository AND the text file.
4. Results of the analysis.
5. Scripts to create the Postgres SQL database and tables (if applicable).
6. You will be asked to add an instructor Github account to your repo closer to the date. Submissions which do not do this before the deadline **will not** be graded.


Your submission will be run against different (and larger) JSON files.

