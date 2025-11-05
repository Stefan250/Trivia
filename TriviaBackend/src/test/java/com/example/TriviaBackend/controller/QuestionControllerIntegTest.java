package com.example.TriviaBackend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TriviaBackend.dto.response.TriviaResponse;
import com.example.TriviaBackend.entity.QuestionEntity;
import com.example.TriviaBackend.repository.QuestionRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionControllerIntegTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private QuestionRepository questionRepository;

  @MockitoBean private RestTemplate restTemplate;

  @Nested
  class GetQuestions {

    @Test
    void givenTwoQuestions_whenRequestingQuestions_returnsResultAndSaves() throws Exception {
      final TriviaResponse response =
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
          .thenReturn(response);

      mockMvc
          .perform(get("/api/questions"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.questionResponse.length()").value(2))
          .andExpect(
              jsonPath("$.questionResponse[0].question")
                  .value("what is the best programming language?"))
          .andExpect(
              jsonPath("$.questionResponse[1].question").value("How do you start a new project?"));

      final List<QuestionEntity> savedQuestions = questionRepository.findAll();

      Assertions.assertThat(savedQuestions).hasSize(2);
      Assertions.assertThat(savedQuestions)
          .extracting(QuestionEntity::getQuestion)
          .containsExactlyInAnyOrder(
              "what is the best programming language?", "How do you start a new project?");

      Mockito.verify(restTemplate)
          .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
      Mockito.verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void givenZeroQuestions_whenRequestingQuestions_returnsEmptyAndSavesNothing() throws Exception {
      Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
          .thenReturn(null);

      mockMvc
          .perform(get("/api/questions"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.questionResponse.length()").value(0));

      final List<QuestionEntity> savedQuestions = questionRepository.findAll();

      Assertions.assertThat(savedQuestions).hasSize(0);

      Mockito.verify(restTemplate)
          .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
      Mockito.verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void givenErrorIsThrown_whenRequestingQuestions_returnsEmptyAndSavesNothing() throws Exception {
      final TriviaResponse response = new TriviaResponse(5, List.of());

      Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
          .thenReturn(response);

      mockMvc.perform(get("/api/questions")).andExpect(status().is4xxClientError());

      final List<QuestionEntity> savedQuestions = questionRepository.findAll();

      Assertions.assertThat(savedQuestions).hasSize(0);

      Mockito.verify(restTemplate)
          .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
      Mockito.verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void givenZeroQuestions_whenRequestingQuestionsResponseCode3_returnsEmptyAndSavesNothing()
        throws Exception {
      final TriviaResponse response = new TriviaResponse(3, List.of());

      Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class)))
          .thenReturn(response);

      mockMvc
          .perform(get("/api/questions"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.questionResponse.length()").value(0));

      final List<QuestionEntity> savedQuestions = questionRepository.findAll();

      Assertions.assertThat(savedQuestions).hasSize(0);

      Mockito.verify(restTemplate)
          .getForObject(Mockito.anyString(), Mockito.eq(TriviaResponse.class));
      Mockito.verifyNoMoreInteractions(restTemplate);
    }
  }

  @Nested
  class CheckAnswers {

    @Test
    void givenTwoQuestions_whenCheckingAnswers_thenReturnResult() throws Exception {
      final QuestionEntity question1 =
          new QuestionEntity(
              0,
              "multiple",
              "hard",
              "programming",
              "what is the best programming language?",
              "Java",
              List.of("C#", "Python", "Scala"));
      final QuestionEntity question2 =
          new QuestionEntity(
              0,
              "multiple",
              "hard",
              "programming",
              "How do you start a new project?",
              "Print 'Hello World!'",
              List.of("Open up IntelliJ", "Read the docu", "Phone a Friend"));

      questionRepository.save(question1);
      questionRepository.save(question2);

      // The ID is generated after saving
      final Long questionId1 = question1.getId();
      final Long questionId2 = question2.getId();

      final String requestBody =
          """
            {
              "answerRequests": [
                {
                  "questionId": "%s",
                  "answer": "Java"
                },
                {
                  "questionId": "%s",
                  "answer": "Read the docu"
                }
              ]
            }
            """
              .formatted(questionId1, questionId2);

      mockMvc
          .perform(
              post("/api/checkanswers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.answerResponse.length()").value(2))
          .andExpect(jsonPath("$.answerResponse[0].questionId").value(questionId1))
          .andExpect(jsonPath("$.answerResponse[0].result").value(true))
          .andExpect(jsonPath("$.answerResponse[1].questionId").value(questionId2))
          .andExpect(jsonPath("$.answerResponse[1].result").value(false));
    }

    @Test
    void givenZeroQuestions_whenCheckingAnswers_thenReturnEmpty() throws Exception {
      final String requestBody =
          """
            {
              "answerRequests": [
                {
                  "questionId": "1",
                  "answer": "Java"
                },
                {
                  "questionId": "2",
                  "answer": "Print 'Hello World!'"
                }
              ]
            }
            """;

      mockMvc
          .perform(
              post("/api/checkanswers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.answerResponse.length()").value(2))
          .andExpect(jsonPath("$.answerResponse[0]").isEmpty())
          .andExpect(jsonPath("$.answerResponse[1]").isEmpty());
    }
  }
}
