package com.example.weathertelegram.repositories;

import com.example.weathertelegram.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByChatId(long chatId);
}
