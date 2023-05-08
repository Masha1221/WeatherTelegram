package com.example.weathertelegram.services;

import com.example.weathertelegram.exceptions.EntityException;
import com.example.weathertelegram.models.PostEntity;
import com.example.weathertelegram.repositories.PostRepository;
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

    public void savePost(PostEntity post){
        postRepository.save(post);
    }
}
