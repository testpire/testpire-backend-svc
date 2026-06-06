package com.testpire.testpire.dto.response;

import com.testpire.testpire.enums.DifficultyLevel;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class QuestionResponseDto {
  Long id;
  String externalId;
  String text;
  String questionImagePath;
  DifficultyLevel difficultyLevel;
  Long topicId;
  String topicName;
  Long correctOptionId;
  Long instituteId;
  String instituteName;
  String questionType;
  Integer marks;
  Integer negativeMarks;
  String explanation;
  List<OptionResponseDto> options;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  Boolean active;
}


