package com.example.battle_bot.repositories;

import com.example.battle_bot.models.BonusCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusCodeRepository extends JpaRepository<BonusCodeEntity, Long> {
}
