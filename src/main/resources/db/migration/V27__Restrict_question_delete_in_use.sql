-- Protect test results and live test contents from question hard-deletes.
--
-- V23 created both references to a question as ON DELETE CASCADE:
--   test_questions.question_id       -> questions(id)  ON DELETE CASCADE
--   test_attempt_answers.question_id -> questions(id)  ON DELETE CASCADE
-- and V26 added ON DELETE CASCADE all the way up the questions -> topics -> ... -> institutes chain.
--
-- Combined, deleting a question (directly, or as a side effect of deleting a Topic/Institute) would
-- silently strip it out of PUBLISHED tests and DESTROY the recorded answers behind already-graded
-- attempts (test_attempts.score then no longer reconciles with the surviving answer rows).
--
-- A foreign key cannot distinguish "published" from "draft", and the dangerous deletes include the
-- cascade from a parent row, which bypasses any application-level guard. So the only airtight fix is
-- at the FK level: switch both question references to ON DELETE RESTRICT. A question that is in any
-- test or has any recorded answer can no longer be deleted (nor can its parent topic/institute) until
-- those references are removed first.

ALTER TABLE test_questions DROP CONSTRAINT IF EXISTS fk_test_questions_question;
ALTER TABLE test_questions ADD  CONSTRAINT fk_test_questions_question
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE RESTRICT;

ALTER TABLE test_attempt_answers DROP CONSTRAINT IF EXISTS fk_attempt_answers_question;
ALTER TABLE test_attempt_answers ADD  CONSTRAINT fk_attempt_answers_question
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE RESTRICT;
