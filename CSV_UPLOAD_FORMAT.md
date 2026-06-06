# CSV Upload Format for Questions

This document describes the CSV format for bulk uploading questions to the system.

> This format matches the parser in `service/CsvUploadService.createQuestionFromCsvRow`. The first
> line of the file is always treated as a **header and skipped**.

## CSV Structure

Each row is one question followed by its options. The columns, **in order**, are:

| # | Column | Required | Notes |
|---|--------|----------|-------|
| 0 | **Question Id** | Yes | Your own string id for the question (e.g. `Q01`). Drives idempotent re-uploads (see below). Must be unique within a single file. |
| 1 | Question Text | Yes | Wrap in double quotes if it contains commas. |
| 2 | Question Image URL | No | Leave empty (`""`) for none. If set, the image is downloaded and re-hosted to S3 (see Notes). |
| 3 | Difficulty Level | Yes | One of `EASY`, `MEDIUM`, `HARD`, `ALL`. Empty or unrecognized values reject the row. |
| 4 | Question Type | Yes | e.g. `MCQ`, `TRUE_FALSE`. Required (empty rejects the row). |
| 5 | Marks | No | **Integer.** Empty defaults to `1`; a non-integer value rejects the row. |
| 6 | Negative Marks | No | **Integer** (use `0`, not `0.5`). Empty defaults to `0`; a non-integer value rejects the row. |
| 7 | Explanation | No | |
| 8 | **Topic ID** | Yes | Numeric ID of the topic. Must belong to your institute. A non-numeric value fails the row. |
| 9 | Option1 Text | Yes | |
| 10 | Option1 Image URL | No | |
| 11 | Option1 IsCorrect | Yes | `true` / `1` / `yes` = correct (case-insensitive); anything else = not correct. |
| 12 | Option2 Text | Yes | |
| 13 | Option2 Image URL | No | |
| 14 | Option2 IsCorrect | Yes | |
| 15+ | Option3 … (Text, Image URL, IsCorrect) | No | Options continue in repeating **triples**. |
| 18+ | Option4 … (Text, Image URL, IsCorrect) | No | |

**Institute ID is not a column** — it is resolved from your authentication token, not the CSV.

### Question Id & idempotency

The **Question Id** (column 0) is your own stable identifier for each question. The server prefixes it
with your **institute code** to form the stored external id (e.g. id `Q01` for institute `ABC` is
stored as `ABC_Q01`; if the institute has no code on record, its numeric id is used instead). Because
of the prefix, two institutes can independently use the same ids without colliding.

> Questions created via the regular `POST /api/questions` endpoint (no Question Id) are auto-assigned
> an external id of the same form: `<institute code>_<generated question id>`.

This makes the upload **idempotent**: if a row's Question Id already exists for your institute, that
question is **updated in place** (text, fields, and options are replaced) instead of creating a
duplicate. Re-running the same file therefore converges to the same state — safe to retry after a
partial failure, or to push edits by re-uploading. A Question Id that is **blank rejects the row**, and
a Question Id that appears **twice in the same file** rejects the second occurrence.

Options are parsed as repeating `(Text, Image URL, IsCorrect)` triples starting at column 9, so a
complete triple is required for each option (provide all three sub-columns, leaving Image URL empty
if there is no image). An entirely empty trailing triple is ignored; a triple with an image or
IsCorrect value but **no text** rejects the row.

## Validation

The header row is validated **before any data is processed**. If the fixed columns are missing,
renamed, or out of order, or the option columns don't come in complete groups of three, the **entire
upload is rejected** with a single descriptive error (no rows are imported). This prevents silent
column-drift where, e.g., an option's text gets mis-read as the Topic ID.

Each data row is then validated independently; an invalid row is skipped and reported in the
`errors` list (prefixed with its row number) while valid rows still import. A row is rejected when:

- Question Id is missing, or is a duplicate of another Question Id earlier in the same file.
- Question Text, Difficulty Level, Question Type, or Topic ID is missing.
- Difficulty Level is not one of `EASY`, `MEDIUM`, `HARD`, `ALL`.
- Marks or Negative Marks is present but not a whole number.
- Topic ID is not a number.
- Fewer than two options are provided, or no option is marked correct.
- An option `IsCorrect` value is not one of `true/false/1/0/yes/no`.

Image-fetch failures are **not** fatal: the row still imports and the failure is reported in `errors`
as a `Row N (warning): ...` entry.

## Example CSV Content

```csv
Question Id,Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,Explanation,Topic ID,Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text,Option2 Image URL,Option2 IsCorrect,Option3 Text,Option3 Image URL,Option3 IsCorrect,Option4 Text,Option4 Image URL,Option4 IsCorrect
"Q01","What is the SI unit of force?","","EASY","MCQ","1","0","Force is measured in newtons (N).","1","Newton","","true","Joule","","false","Pascal","","false","Watt","","false"
"Q02","A body moves with uniform acceleration. Identify the correct v-t graph.","https://upload.wikimedia.org/wikipedia/commons/3/3f/Velocity-time_graph.png","MEDIUM","MCQ","4","1","Uniform acceleration gives a straight sloped line.","1","Graph A","https://example.com/opt-a.png","true","Graph B","https://example.com/opt-b.png","false","Graph C","","false","Graph D","","false"
"Q03","Two charges are separated by distance r. The force between them is governed by which law?","","HARD","MCQ","4","1","Coulomb's law: F = k q1 q2 / r^2.","1","Coulomb's Law","","true","Ohm's Law","","false","Lenz's Law","","false","Faraday's Law","","false"
```

Replace the `Topic ID` value (`1` above) with a real topic ID in your institute. See
`sample-questions.csv` in the repo root for a ready-to-upload copy.

## Notes

1. **Image URLs**: If provided, the image is downloaded and uploaded to AWS S3 under
   `institute_<id>/<course>/<chapter>/<topic>/...` (option images under an `options/` subfolder). The
   database stores the **S3 object key** (not the full URL); API responses render the full public URL.
   For security, the fetch rejects non-`http(s)` URLs and hosts that resolve to internal/private
   addresses. A failed image fetch is non-fatal — the question is still created with an empty image.

2. **Marks / Negative Marks**: Both are integers. Decimal values (e.g. `0.5`) are not supported; an
   empty value uses the default (`1` / `0`), but a non-integer value rejects the row.

3. **Correct Answer**: At least one option **must** be marked correct, or the row fails. If more than
   one is marked correct, only the **first** correct option is recorded as the question's canonical
   correct answer (`correctOptionId`).

4. **Difficulty Levels**: One of `EASY`, `MEDIUM`, `HARD`, `ALL`. An empty or unrecognized value
   rejects the row.

5. **Boolean Values**: For IsCorrect columns, `true` / `1` / `yes` (case-insensitive) mean correct;
   everything else (including `false` / `0` / `no` / blank) means not correct.

6. **CSV Format**: Standard comma separators. Wrap any field containing a comma in double quotes.

## API Endpoint

**POST** `/api/questions/bulk-upload`

**Auth:** Requires a Bearer token with role `SUPER_ADMIN`, `INST_ADMIN`, or `TEACHER`.

**Parameters:**
- `file`: CSV file (`multipart/form-data`).

The institute is taken from your token; the topic for each question is taken from the **Topic ID**
column of that row. There are no `topicId` / `instituteId` request parameters.

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
      "Row 3: For input string: \"abc\"",
      "Row 7: At least one option must be marked as correct"
    ],
    "uploadedQuestions": [...]
  }
}
```
