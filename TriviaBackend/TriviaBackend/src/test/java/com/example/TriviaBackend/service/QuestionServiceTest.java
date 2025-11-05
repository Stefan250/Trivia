package com.example.TriviaBackend.service;

import com.example.TriviaBackend.client.TriviaClient;
import com.example.TriviaBackend.dto.request.AnswerRequest;
import com.example.TriviaBackend.dto.request.CheckAnswersRequest;
import com.example.TriviaBackend.dto.response.AnswerResponse;
import com.example.TriviaBackend.dto.response.QuestionResponse;
import com.example.TriviaBackend.entity.QuestionEntity;
import com.example.TriviaBackend.exception.RateLimitExceededException;
import com.example.TriviaBackend.repository.QuestionRepository;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class QuestionServiceTest {
  private final TriviaClient triviaClient = Mockito.mock();
  private final QuestionRepository questionRepository = Mockito.mock();

  private final QuestionService sut = new QuestionService(questionRepository, triviaClient);

  @Nested
  class GetQuestions {

    @Test
    void givenTwoQuestions_whenRequestingQuestions_thenReturnResults() {
      final List<QuestionEntity> questions =
          List.of(
              new QuestionEntity(
                  1,
                  "multiple",
                  "hard",
                  "programming",
                  "what is the best programming language?",
                  "Java",
                  List.of("C#", "Python", "Scala")),
              new QuestionEntity(
                  2,
                  "multiple",
                  "hard",
                  "programming",
                  "How do you start a new project?",
                  "Print 'Hello World!'",
                  List.of("Open up IntelliJ", "Read the docu", "Phone a Friend")));

      Mockito.when(triviaClient.getQuestions()).thenReturn(questions);
      Mockito.when(questionRepository.saveAll(Mockito.any())).thenReturn(questions);

      final var result = sut.getQuestions();

      Assertions.assertThat(result.questionResponse())
          .isNotNull()
          .satisfiesExactly(
              question ->
                  Assertions.assertThat(question)
                      .isEqualTo(
                          new QuestionResponse(
                              1,
                              "what is the best programming language?",
                              List.of("C#", "Python", "Scala", "Java"))),
              question ->
                  Assertions.assertThat(question)
                      .isEqualTo(
                          new QuestionResponse(
                              2,
                              "How do you start a new project?",
                              List.of(
                                  "Open up IntelliJ",
                                  "Read the docu",
                                  "Phone a Friend",
                                  "Print 'Hello World!'"))));

      Mockito.verify(triviaClient).getQuestions();
      Mockito.verify(questionRepository).saveAll(questions);
      Mockito.verifyNoMoreInteractions(triviaClient);
      Mockito.verifyNoMoreInteractions(questionRepository);
    }

    @Test
    void givenNoQuestions_whenRequestingQuestions_thenReturnEmptyResult() {
      Mockito.when(triviaClient.getQuestions()).thenReturn(List.of());
      Mockito.when(questionRepository.saveAll(Mockito.any())).thenReturn(null);

      final var result = sut.getQuestions();

      Assertions.assertThat(result.questionResponse()).isEmpty();

      Mockito.verify(triviaClient).getQuestions();
      Mockito.verify(questionRepository).saveAll(Mockito.any());
      Mockito.verifyNoMoreInteractions(triviaClient);
      Mockito.verifyNoMoreInteractions(questionRepository);
    }

    @Test
    void givenRateLimitReached_whenRequestingQuestions_thenReturnError() {
      Mockito.when(triviaClient.getQuestions()).thenThrow(new RuntimeException());

      Assertions.assertThatThrownBy(sut::getQuestions)
          .isInstanceOf(RateLimitExceededException.class)
          .hasMessageContaining("Please wait 5 seconds before retrying");

      Mockito.verify(questionRepository, Mockito.never()).saveAll(Mockito.any());
      Mockito.verify(triviaClient).getQuestions();
      Mockito.verifyNoMoreInteractions(triviaClient);
    }
  }

  @Nested
  class CheckAnswer {

    @Test
    void givenTwoAnswers_whenCheckingAnswers_thenReturnResults() {
      final List<QuestionEntity> questions =
          List.of(
              new QuestionEntity(
                  1,
                  "multiple",
                  "hard",
                  "programming",
                  "what is the best programming language?",
                  "Java",
                  List.of("C#", "Python", "Scala")),
              new QuestionEntity(
                  2,
                  "multiple",
                  "hard",
                  "programming",
                  "How do you start a new project?",
                  "Print 'Hello World!'",
                  List.of("Open up IntelliJ", "Read the docu", "Phone a Friend")));
      final CheckAnswersRequest checkAnswersRequest =
          new CheckAnswersRequest(
              List.of(new AnswerRequest(1, "Java"), new AnswerRequest(2, "Open up IntelliJ")));

      Mockito.when(questionRepository.findById(1L)).thenReturn(Optional.of(questions.getFirst()));
      Mockito.when(questionRepository.findById(2L)).thenReturn(Optional.of(questions.get(1)));

      final var result = sut.checkAnswer(checkAnswersRequest);

      Assertions.assertThat(result.answerResponse())
          .isNotNull()
          .satisfiesExactly(
              answer -> Assertions.assertThat(answer).isEqualTo(new AnswerResponse(1, true)),
              answer -> Assertions.assertThat(answer).isEqualTo(new AnswerResponse(2, false)));

      Mockito.verify(questionRepository, Mockito.times(2)).findById(Mockito.any());
      Mockito.verifyNoMoreInteractions(questionRepository);
    }

    @Test
    void givenTwoAnswers_whenCannotFindQuestions_thenReturnEmtpyResult() {
      final CheckAnswersRequest checkAnswersRequest =
          new CheckAnswersRequest(
              List.of(new AnswerRequest(1, "Java"), new AnswerRequest(2, "Open up IntelliJ")));

      Mockito.when(questionRepository.findById(1L)).thenReturn(Optional.empty());
      Mockito.when(questionRepository.findById(2L)).thenReturn(Optional.empty());

      final var result = sut.checkAnswer(checkAnswersRequest);

      Assertions.assertThat(result.answerResponse())
          .isNotNull()
          .satisfiesExactly(
              answer -> Assertions.assertThat(answer).isNull(),
              answer -> Assertions.assertThat(answer).isNull());

      Mockito.verify(questionRepository, Mockito.times(2)).findById(Mockito.any());
      Mockito.verifyNoMoreInteractions(questionRepository);
    }
  }
}
