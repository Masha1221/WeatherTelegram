package com.example.battle_bot.repositories;

import com.example.battle_bot.models.BinaryContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContentEntity,Long> {
}
