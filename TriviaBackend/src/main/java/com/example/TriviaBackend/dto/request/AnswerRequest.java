package com.example.TriviaBackend.dto.request;

public record AnswerRequest(long questionId, String answer) {
}
