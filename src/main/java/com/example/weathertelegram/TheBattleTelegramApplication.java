package com.example.weathertelegram;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UserAction;
import com.github.instagram4j.instagram4j.actions.users.UsersAction;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.models.user.User;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUsersResponse;
import com.github.instagram4j.instagram4j.responses.users.UsersSearchResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SpringBootApplication
public class TheBattleTelegramApplication {
    public static void main(String[] args) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        SpringApplication.run(TheBattleTelegramApplication.class, args);
    }
}


