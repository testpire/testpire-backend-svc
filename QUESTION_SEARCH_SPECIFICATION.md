# Question Search Specification Documentation

## Overview

The Question Repository now supports advanced search functionality using JPA Criteria API specifications. This provides type-safe, flexible, and performant querying capabilities for questions with multiple filtering criteria.

## Features

### 1. QuestionSpecification Class

The `QuestionSpecification` class provides static methods for building JPA Criteria API specifications. Each method returns a `Specification<Question>` that can be combined with other specifications.

### 2. Available Search Criteria

#### Basic Filters
- **Institute ID**: Filter by specific institute
- **Course ID**: Filter by course (through topic hierarchy)
- **Subject ID**: Filter by subject (through topic hierarchy)
- **Chapter ID**: Filter by chapter (through topic hierarchy)
- **Topic ID**: Filter by specific topic

#### Question Content Filters
- **Search Text**: Search in question text, question type, and explanation
- **Difficulty Level**: Filter by EASY, MEDIUM, HARD
- **Question Type**: Filter by question type (MCQ, TRUE_FALSE, etc.)

#### Marks and Scoring
- **Marks Range**: Filter by minimum and maximum marks
- **Negative Marks Range**: Filter by minimum and maximum negative marks

#### Status and Metadata
- **Active Status**: Filter by active/inactive questions
- **Has Question Image**: Filter questions that have images
- **Has Explanation**: Filter questions that have explanations
- **Has Correct Option**: Filter questions with correct options defined
- **Has Options**: Filter questions that have options
- **Options Count**: Filter by minimum/maximum number of options

#### Temporal Filters
- **Created After**: Filter questions created after a specific date
- **Created Before**: Filter questions created before a specific date
- **Created By**: Filter questions created by a specific user

### 3. Pagination and Sorting

The search supports:
- **Page-based pagination**: Page number and size
- **Sorting**: Sort by any field in ascending or descending order
- **Default sorting**: By creation date (descending)

## API Endpoints

### 1. POST /api/questions/search/advanced

Advanced search using request body with full criteria support.

**Request Body Example:**
```json
{
  "instituteId": 1,
  "courseId": 1,
  "subjectId": 1,
  "chapterId": 1,
  "topicId": 1,
  "searchText": "What is",
  "difficultyLevel": "EASY",
  "questionType": "MCQ",
  "minMarks": 1,
  "maxMarks": 10,
  "active": true,
  "hasQuestionImage": true,
  "hasExplanation": true,
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

### 2. GET /api/questions/search/advanced

Advanced search using query parameters (simplified version).

**Query Parameters:**
- `instituteId` (required): Institute ID
- `courseId` (optional): Course ID
- `subjectId` (optional): Subject ID
- `chapterId` (optional): Chapter ID
- `topicId` (optional): Topic ID
- `searchText` (optional): Search text
- `difficultyLevel` (optional): Difficulty level
- `questionType` (optional): Question type
- `minMarks` (optional): Minimum marks
- `maxMarks` (optional): Maximum marks
- `active` (optional): Active status
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sortBy` (optional, default: "createdAt"): Sort field
- `sortDirection` (optional, default: "desc"): Sort direction

**Example URL:**
```
GET /api/questions/search/advanced?instituteId=1&courseId=1&difficultyLevel=EASY&searchText=What&page=0&size=10
```

## Usage Examples

### 1. Basic Search by Institute
```java
QuestionSearchRequestDto request = QuestionSearchRequestDto.builder()
    .instituteId(1L)
    .active(true)
    .build();

QuestionListResponseDto result = questionService.searchQuestionsWithSpecification(request);
```

### 2. Search by Course and Difficulty
```java
QuestionSearchRequestDto request = QuestionSearchRequestDto.builder()
    .instituteId(1L)
    .courseId(1L)
    .difficultyLevel(DifficultyLevel.EASY)
    .active(true)
    .build();

QuestionListResponseDto result = questionService.searchQuestionsWithSpecification(request);
```

### 3. Text Search with Pagination
```java
QuestionSearchRequestDto request = QuestionSearchRequestDto.builder()
    .instituteId(1L)
    .searchText("mathematics")
    .page(0)
    .size(10)
    .sortBy("createdAt")
    .sortDirection("desc")
    .build();

QuestionListResponseDto result = questionService.searchQuestionsWithSpecification(request);
```

### 4. Complex Search with Multiple Criteria
```java
QuestionSearchRequestDto request = QuestionSearchRequestDto.builder()
    .instituteId(1L)
    .courseId(1L)
    .subjectId(1L)
    .chapterId(1L)
    .topicId(1L)
    .searchText("algebra")
    .difficultyLevel(DifficultyLevel.MEDIUM)
    .questionType("MCQ")
    .minMarks(5)
    .maxMarks(15)
    .hasQuestionImage(true)
    .hasExplanation(true)
    .active(true)
    .page(0)
    .size(20)
    .sortBy("marks")
    .sortDirection("asc")
    .build();

QuestionListResponseDto result = questionService.searchQuestionsWithSpecification(request);
```

## Response Format

All search endpoints return a standardized response:

```json
{
  "message": "Questions retrieved successfully",
  "success": true,
  "data": {
    "questions": [
      {
        "id": 1,
        "text": "What is the capital of France?",
        "questionImagePath": "https://s3.../question1.jpg",
        "difficultyLevel": "EASY",
        "topicId": 1,
        "topicName": "Geography",
        "correctOptionId": 1,
        "instituteId": 1,
        "questionType": "MCQ",
        "marks": 5,
        "negativeMarks": 1,
        "explanation": "Paris is the capital of France",
        "options": [
          {
            "id": 1,
            "text": "Paris",
            "optionImagePath": null,
            "questionId": 1,
            "optionOrder": 1,
            "isCorrect": true,
            "createdAt": "2024-01-01T10:00:00",
            "updatedAt": "2024-01-01T10:00:00",
            "createdBy": "teacher1",
            "updatedBy": "teacher1",
            "active": true
          }
        ],
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00",
        "createdBy": "teacher1",
        "updatedBy": "teacher1",
        "active": true
      }
    ],
    "totalCount": 1
  }
}
```

## Performance Considerations

1. **Indexing**: Ensure proper database indexes on frequently searched fields:
   - `institute_id`
   - `topic_id`
   - `difficulty_level`
   - `question_type`
   - `active`
   - `deleted`
   - `created_at`

2. **Pagination**: Always use pagination for large result sets to avoid memory issues.

3. **Selective Fields**: The specification automatically handles joins efficiently, but avoid unnecessary complex criteria combinations.

## Error Handling

The API returns appropriate error responses for:
- Invalid parameters
- Database connection issues
- Authorization failures
- Validation errors

Example error response:
```json
{
  "message": "Failed to search questions: Invalid difficulty level",
  "success": false,
  "data": null
}
```

## Security

- All search endpoints require authentication
- Users can only search questions within their institute (enforced by `instituteId` parameter)
- Role-based access control is applied based on user roles

## Migration from Old Search Methods

The new specification-based search is additive and doesn't break existing functionality. Old search methods are still available:

- `searchQuestions(instituteId, query)`
- `searchQuestionsByTopic(topicId, instituteId, query)`
- `getQuestionsByTopic(topicId, instituteId)`
- `getQuestionsByInstitute(instituteId)`

The new advanced search provides more flexibility and should be preferred for complex search requirements.
