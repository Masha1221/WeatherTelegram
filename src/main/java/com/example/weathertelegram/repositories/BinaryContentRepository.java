package com.example.weathertelegram.repositories;

import com.example.weathertelegram.models.BinaryContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContentEntity,Long> {
}
