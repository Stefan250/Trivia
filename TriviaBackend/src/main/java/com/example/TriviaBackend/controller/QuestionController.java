package com.example.TriviaBackend.controller;

import com.example.TriviaBackend.dto.request.CheckAnswersRequest;
import com.example.TriviaBackend.dto.response.CheckAnswersResponse;
import com.example.TriviaBackend.dto.response.GetQuestionsResponse;
import com.example.TriviaBackend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/")
public class QuestionController {
  private final QuestionService questionService;

  @Autowired
  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping("/questions")
  public GetQuestionsResponse getQuestions() {
    return questionService.getQuestions();
  }

  @PostMapping("/checkanswers")
  public CheckAnswersResponse checkAnswers(@RequestBody CheckAnswersRequest checkAnswersRequest) {
    return questionService.checkAnswer(checkAnswersRequest);
  }
}
