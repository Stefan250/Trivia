package com.example.TriviaBackend.controller;

import com.example.TriviaBackend.dto.request.CheckAnswersRequest;
import com.example.TriviaBackend.dto.response.CheckAnswersResponse;
import com.example.TriviaBackend.dto.response.GetQuestionsResponse;
import com.example.TriviaBackend.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/")
public class QuestionController {
    private final QuestionService questionService;
    private final Logger LOG = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/questions")
    public GetQuestionsResponse getQuestions() {
        LOG.info("/questions endpoint called");
        return questionService.getQuestions();
    }

    @PostMapping("/checkanswers")
    public CheckAnswersResponse checkAnswers(@RequestBody CheckAnswersRequest checkAnswersRequest) {
        LOG.info("/checkanswers endpoint called");
        return questionService.checkAnswer(checkAnswersRequest);
    }
}
