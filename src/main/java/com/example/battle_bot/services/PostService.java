package com.example.battle_bot.services;

import com.example.battle_bot.exceptions.EntityException;
import com.example.battle_bot.models.PostEntity;
import com.example.battle_bot.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostEntity getTheLastPost() {
        long id = postRepository.findAll().stream()
                .mapToLong(PostEntity::getId)
                .max()
                .orElse(0);
        return postRepository.findById(id).orElseThrow(() -> new EntityException("PostEntity not found"));
    }

    public void savePost(PostEntity post) {
        postRepository.save(post);
    }
}
