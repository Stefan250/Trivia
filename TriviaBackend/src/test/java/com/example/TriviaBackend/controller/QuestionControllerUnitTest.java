package com.example.TriviaBackend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.TriviaBackend.dto.request.CheckAnswersRequest;
import com.example.TriviaBackend.dto.response.AnswerResponse;
import com.example.TriviaBackend.dto.response.CheckAnswersResponse;
import com.example.TriviaBackend.dto.response.GetQuestionsResponse;
import com.example.TriviaBackend.dto.response.QuestionResponse;
import com.example.TriviaBackend.exception.RateLimitExceededException;
import com.example.TriviaBackend.service.QuestionService;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuestionController.class)
class QuestionControllerUnitTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private QuestionService questionService;

  @Nested
  class GetQuestions {

    @Test
    void givenTwoQuestions_whenRequestingQuestions_returnResult() throws Exception {
      final GetQuestionsResponse response =
          new GetQuestionsResponse(
              List.of(
                  new QuestionResponse(1L, "What is 2 + 2?", List.of("3", "4", "5")),
                  new QuestionResponse(
                      2L, "Capital of France?", List.of("Paris", "Berlin", "Rome"))));

      Mockito.when(questionService.getQuestions()).thenReturn(response);

      mockMvc
          .perform(get("/api/questions"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.questionResponse.length()").value(2))
          .andExpect(jsonPath("$.questionResponse[0].id").value(1))
          .andExpect(jsonPath("$.questionResponse[1].id").value(2))
          .andExpect(jsonPath("$.questionResponse[0].question").value("What is 2 + 2?"))
          .andExpect(jsonPath("$.questionResponse[1].question").value("Capital of France?"));

      Mockito.verify(questionService).getQuestions();
      Mockito.verifyNoMoreInteractions(questionService);
    }

    @Test
    void givenZeroQuestions_whenRequestingQuestions_returnEmpty() throws Exception {
      final GetQuestionsResponse response = new GetQuestionsResponse(List.of());

      Mockito.when(questionService.getQuestions()).thenReturn(response);

      mockMvc
          .perform(get("/api/questions"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.questionResponse.length()").value(0));

      Mockito.verify(questionService).getQuestions();
      Mockito.verifyNoMoreInteractions(questionService);
    }

    @Test
    void givenZeroQuestion_whenRequestingQuestions_returnError() throws Exception {
      Mockito.when(questionService.getQuestions()).thenThrow(RateLimitExceededException.class);

      mockMvc.perform(get("/api/questions")).andExpect(status().is4xxClientError());

      Mockito.verify(questionService).getQuestions();
      Mockito.verifyNoMoreInteractions(questionService);
    }
  }

  @Nested
  class CheckAnswer {

    @Test
    void givenTwoAnswers_whenCheckAnswers_returnResult() throws Exception {
      final CheckAnswersResponse response =
          new CheckAnswersResponse(
              List.of(new AnswerResponse(1L, true), new AnswerResponse(2L, false)));

      Mockito.when(questionService.checkAnswer(Mockito.any(CheckAnswersRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/checkanswers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                        {
                          "answerRequests": [
                              {
                                "questionId": 1,
                                "result": true
                              },
                              {
                                "questionId": 2,
                                "result": false
                              }
                          ]
                        }
                        """))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.answerResponse.length()").value(2))
          .andExpect(jsonPath("$.answerResponse[0].questionId").value(1))
          .andExpect(jsonPath("$.answerResponse[1].questionId").value(2))
          .andExpect(jsonPath("$.answerResponse[0].result").value(true))
          .andExpect(jsonPath("$.answerResponse[1].result").value(false));

      Mockito.verify(questionService).checkAnswer(Mockito.any(CheckAnswersRequest.class));
      Mockito.verifyNoMoreInteractions(questionService);
    }

    @Test
    void givenZeroAnswers_whenCheckAnswers_returnEmpty() throws Exception {
      final CheckAnswersResponse response = new CheckAnswersResponse(List.of());

      Mockito.when(questionService.checkAnswer(Mockito.any(CheckAnswersRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(post("/api/checkanswers").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.answerResponse.length()").value(0));

      Mockito.verify(questionService).checkAnswer(Mockito.any(CheckAnswersRequest.class));
      Mockito.verifyNoMoreInteractions(questionService);
    }

    @Test
    void givenTwoAnswers_whenCheckAnswersAnswersNotFound_returnEmpty() throws Exception {
      final CheckAnswersResponse response = new CheckAnswersResponse(List.of());

      Mockito.when(questionService.checkAnswer(Mockito.any(CheckAnswersRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/checkanswers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                        {
                          "answerRequests": [
                              {
                                "questionId": 1,
                                "result": true
                              },
                              {
                                "questionId": 2,
                                "result": false
                              }
                          ]
                        }
                        """))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.answerResponse.length()").value(0));

      Mockito.verify(questionService).checkAnswer(Mockito.any(CheckAnswersRequest.class));
      Mockito.verifyNoMoreInteractions(questionService);
    }
  }
}
