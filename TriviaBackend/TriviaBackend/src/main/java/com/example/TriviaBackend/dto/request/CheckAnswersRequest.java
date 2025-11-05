package com.example.TriviaBackend.dto.request;

import java.util.List;

public record CheckAnswersRequest(List<AnswerRequest> answerRequests) {
}
