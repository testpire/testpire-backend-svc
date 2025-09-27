# CSV Upload Format for Questions

This document describes the CSV format for bulk uploading questions to the system.

## CSV Structure

The CSV file should have the following columns in order:

| Column | Description | Required | Example |
|--------|-------------|----------|---------|
| Question Text | The main question text | Yes | "What is the capital of France?" |
| Question Image URL | URL to the question image (optional) | No | "https://example.com/question.jpg" |
| Difficulty Level | EASY, MEDIUM, HARD, or ALL | Yes | "EASY" |
| Question Type | Type of question (e.g., MCQ, TRUE_FALSE) | Yes | "MCQ" |
| Marks | Points for correct answer | Yes | "1" |
| Negative Marks | Points deducted for wrong answer | Yes | "0" |
| Explanation | Explanation for the answer (optional) | No | "Paris is the capital of France" |
| Option1 Text | First option text | Yes | "Paris" |
| Option1 Image URL | URL to first option image (optional) | No | "https://example.com/option1.jpg" |
| Option1 IsCorrect | Whether first option is correct (true/false/1/0/yes/no) | Yes | "true" |
| Option2 Text | Second option text | Yes | "London" |
| Option2 Image URL | URL to second option image (optional) | No | "https://example.com/option2.jpg" |
| Option2 IsCorrect | Whether second option is correct | Yes | "false" |
| Option3 Text | Third option text (optional) | No | "Berlin" |
| Option3 Image URL | URL to third option image (optional) | No | "https://example.com/option3.jpg" |
| Option3 IsCorrect | Whether third option is correct | No | "false" |
| Option4 Text | Fourth option text (optional) | No | "Madrid" |
| Option4 Image URL | URL to fourth option image (optional) | No | "https://example.com/option4.jpg" |
| Option4 IsCorrect | Whether fourth option is correct | No | "false" |

## Example CSV Content

```csv
Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,Explanation,Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text,Option2 Image URL,Option2 IsCorrect,Option3 Text,Option3 Image URL,Option3 IsCorrect,Option4 Text,Option4 Image URL,Option4 IsCorrect
"What is the capital of France?","https://example.com/france.jpg","EASY","MCQ","1","0","Paris is the capital and largest city of France","Paris","https://example.com/paris.jpg","true","London","https://example.com/london.jpg","false","Berlin","https://example.com/berlin.jpg","false","Madrid","https://example.com/madrid.jpg","false"
"Which planet is closest to the Sun?","","MEDIUM","MCQ","2","0.5","Mercury is the smallest planet and closest to the Sun","Mercury","","true","Venus","","false","Earth","","false","Mars","","false"
"What is 2 + 2?","","EASY","MCQ","1","0","Basic arithmetic","4","","true","3","","false","5","","false","6","","false"
```

## Notes

1. **Image URLs**: If provided, images will be automatically downloaded and uploaded to AWS S3. The original URLs will be replaced with S3 URLs in the database.

2. **Option Count**: You can have between 2 and 6 options per question. If you have fewer than 6 options, leave the extra columns empty.

3. **Correct Answer**: At least one option must be marked as correct. You can have multiple correct answers if needed.

4. **Difficulty Levels**: Must be one of: EASY, MEDIUM, HARD, ALL

5. **Boolean Values**: For IsCorrect columns, you can use: true/false, 1/0, yes/no (case insensitive)

6. **CSV Format**: Use standard CSV format with comma separators. If your text contains commas, wrap it in double quotes.

## API Endpoint

**POST** `/api/question/bulk-upload`

**Parameters:**
- `file`: CSV file (multipart/form-data)
- `topicId`: Long - ID of the topic to add questions to
- `instituteId`: Long - ID of the institute

**Response:**
```json
{
  "message": "Bulk upload completed",
  "success": true,
  "data": {
    "totalProcessed": 10,
    "successfulUploads": 8,
    "failedUploads": 2,
    "errors": [
      "Row 3: Invalid difficulty level",
      "Row 7: Missing question text"
    ],
    "uploadedQuestions": [...]
  }
}
```


