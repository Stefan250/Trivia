package com.example.TriviaBackend.dto.response;

import java.util.List;

public record GetQuestionsResponse(List<QuestionResponse> questionResponse) {}
