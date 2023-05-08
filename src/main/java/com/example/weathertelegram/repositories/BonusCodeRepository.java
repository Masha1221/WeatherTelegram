package com.example.weathertelegram.repositories;

import com.example.weathertelegram.models.BonusCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusCodeRepository extends JpaRepository<BonusCodeEntity, Long> {
}
