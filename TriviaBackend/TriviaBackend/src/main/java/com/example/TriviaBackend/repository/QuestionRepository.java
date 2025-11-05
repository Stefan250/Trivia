package com.example.TriviaBackend.repository;

import com.example.TriviaBackend.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
}
