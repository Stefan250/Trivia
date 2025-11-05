package com.example.TriviaBackend.entity;

import com.example.TriviaBackend.dto.response.TriviaResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.List;
import java.util.Objects;

@Entity
public class QuestionEntity {
  @Id @GeneratedValue private long id;
  private String type;
  private String difficulty;
  private String category;
  private String question;
  private String correctAnswer;
  private List<String> incorrectAnswers;

  public QuestionEntity() {}

  public QuestionEntity(
      long id,
      String type,
      String difficulty,
      String category,
      String question,
      String correctAnswer,
      List<String> incorrectAnswers) {
    this.id = id;
    this.type = type;
    this.difficulty = difficulty;
    this.category = category;
    this.question = question;
    this.correctAnswer = correctAnswer;
    this.incorrectAnswers = incorrectAnswers;
  }

  public QuestionEntity(TriviaResponse.TriviaQuestion triviaQuestion) {
    this.type = triviaQuestion.type();
    this.difficulty = triviaQuestion.difficulty();
    this.category = triviaQuestion.category();
    this.question = triviaQuestion.question();
    this.correctAnswer = triviaQuestion.correctAnswer();
    this.incorrectAnswers = triviaQuestion.incorrectAnswers();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getCorrectAnswer() {
    return correctAnswer;
  }

  public void setCorrectAnswer(String correctAnswer) {
    this.correctAnswer = correctAnswer;
  }

  public List<String> getIncorrectAnswers() {
    return incorrectAnswers;
  }

  public void setIncorrectAnswers(List<String> incorrectAnswers) {
    this.incorrectAnswers = incorrectAnswers;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    QuestionEntity questionEntity1 = (QuestionEntity) o;
    return id == questionEntity1.id
        && Objects.equals(type, questionEntity1.type)
        && Objects.equals(difficulty, questionEntity1.difficulty)
        && Objects.equals(category, questionEntity1.category)
        && Objects.equals(question, questionEntity1.question)
        && Objects.equals(correctAnswer, questionEntity1.correctAnswer)
        && Objects.equals(incorrectAnswers, questionEntity1.incorrectAnswers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, difficulty, category, question, correctAnswer, incorrectAnswers);
  }
}
