package com.example.TriviaBackend.service;

import com.example.TriviaBackend.client.TriviaClient;
import com.example.TriviaBackend.dto.request.CheckAnswersRequest;
import com.example.TriviaBackend.dto.response.AnswerResponse;
import com.example.TriviaBackend.dto.response.CheckAnswersResponse;
import com.example.TriviaBackend.dto.response.GetQuestionsResponse;
import com.example.TriviaBackend.dto.response.QuestionResponse;
import com.example.TriviaBackend.entity.QuestionEntity;
import com.example.TriviaBackend.exception.RateLimitExceededException;
import com.example.TriviaBackend.repository.QuestionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
  private final Logger LOG = LoggerFactory.getLogger(QuestionService.class);
  private final QuestionRepository questionRepository;
  private final TriviaClient triviaClient;

  @Autowired
  public QuestionService(QuestionRepository questionRepository, TriviaClient triviaClient) {
    this.questionRepository = questionRepository;
    this.triviaClient = triviaClient;
  }

  public GetQuestionsResponse getQuestions() {
    try {
      List<QuestionEntity> questionEntities = triviaClient.getQuestions();
      questionRepository.saveAll(questionEntities);
      LOG.info("Saved {} questions to the database", questionEntities.size());
      return new GetQuestionsResponse(
          questionEntities.stream()
              .map(
                  questionEntity ->
                      new QuestionResponse(
                          questionEntity.getId(),
                          questionEntity.getQuestion(),
                          Stream.concat(
                                  questionEntity.getIncorrectAnswers().stream(),
                                  Stream.of(questionEntity.getCorrectAnswer()))
                              .toList()))
              .toList());
    } catch (Exception e) {
      LOG.error("Trivia API rate limit exceeded, throwing RateLimitExceededException");
      throw new RateLimitExceededException();
    }
  }

  public CheckAnswersResponse checkAnswer(CheckAnswersRequest checkAnswersRequest) {
    return new CheckAnswersResponse(
        checkAnswersRequest.answerRequests().stream()
            .map(
                answer -> {
                  Optional<QuestionEntity> result =
                      questionRepository.findById(answer.questionId());
                  if (result.isEmpty()) {
                    LOG.warn("Could not find question with id {} in database", answer.questionId());
                    return null;
                  }

                  if (result.get().getCorrectAnswer().equals(answer.answer())) {
                    return new AnswerResponse(answer.questionId(), true);
                  } else {
                    return new AnswerResponse(answer.questionId(), false);
                  }
                })
            .toList());
  }
}
