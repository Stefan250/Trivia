package com.example.TriviaBackend.client;

import com.example.TriviaBackend.dto.response.TriviaResponse;
import com.example.TriviaBackend.entity.QuestionEntity;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

class TriviaClientTest {
  private final RestTemplate restTemplate = Mockito.mock();
  private final TriviaClient sut = new TriviaClient(restTemplate);

  @Test
  void givenTwoQuestions_whenRequestingQuestionsResponseCode0_returnsResult() {
    final TriviaResponse triviaResponse =
        new TriviaResponse(
            0,
            List.of(
                new TriviaResponse.TriviaQuestion(
                    "multiple",
                    "hard",
                    "programming",
                    "what is the best programming language?",
                    "Java",
                    List.of("C#", "Python", "Scala")),
                new TriviaResponse.TriviaQuestion(
                    "multiple",
                    "hard",
                    "programming",
                    "How do you start a new project?",
                    "Print 'Hello World!'",
                    List.of("Open up IntelliJ", "Read the docu", "Phone a Friend"))));

    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
        .thenReturn(triviaResponse);

    final var result = sut.getQuestions();

    Assertions.assertThat(result)
        .isNotNull()
        .satisfiesExactly(
            question ->
                Assertions.assertThat(question)
                    .isEqualTo(
                        new QuestionEntity(
                            0,
                            "multiple",
                            "hard",
                            "programming",
                            "what is the best programming language?",
                            "Java",
                            List.of("C#", "Python", "Scala"))),
            question ->
                Assertions.assertThat(question)
                    .isEqualTo(
                        new QuestionEntity(
                            0,
                            "multiple",
                            "hard",
                            "programming",
                            "How do you start a new project?",
                            "Print 'Hello World!'",
                            List.of("Open up IntelliJ", "Read the docu", "Phone a Friend"))));

    Mockito.verify(restTemplate)
        .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
    Mockito.verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void givenZeroQuestions_whenRequestingQuestionsResponseCode5_returnsError() {
    final TriviaResponse triviaResponse = new TriviaResponse(5, List.of());

    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
        .thenReturn(triviaResponse);

    Assertions.assertThatThrownBy(sut::getQuestions)
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Rate limit reached");

    Mockito.verify(restTemplate)
        .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
    Mockito.verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void givenZeroQuestions_whenRequestingQuestionsResponseCode3_returnsEmpty() {
    final TriviaResponse triviaResponse = new TriviaResponse(3, List.of());

    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
        .thenReturn(triviaResponse);

    final var result = sut.getQuestions();
    Assertions.assertThat(result).isNotNull().isEmpty();

    Mockito.verify(restTemplate)
        .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
    Mockito.verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void givenZeroQuestions_whenRequestingQuestions_returnsEmpty() {
    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
        .thenReturn(null);

    final var result = sut.getQuestions();
    Assertions.assertThat(result).isNotNull().isEmpty();

    Mockito.verify(restTemplate)
        .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
    Mockito.verifyNoMoreInteractions(restTemplate);
  }
}
