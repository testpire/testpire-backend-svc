-- Break the questions <-> options FK cycle that blocks question deletion and option rebuilds.
--
-- V12 created two opposing references:
--   options.question_id          -> questions(id)  ON DELETE CASCADE
--   questions.correct_option_id  -> options(id)     (no ON DELETE action => RESTRICT)
--
-- Deleting a question makes Hibernate (orphanRemoval=true on Question.options) delete the child
-- option rows first, but the question row still holds correct_option_id pointing at one of them,
-- so the RESTRICT constraint rejects "delete from options where id=<correct option>":
--   ERROR: update or delete on table "options" violates foreign key constraint
--          "fk_questions_correct_option" on table "questions"
-- The same violation occurs in the option-rebuild path (updateQuestion / CSV re-upload), which
-- deletes the existing options while correct_option_id still references one of them.
--
-- correct_option_id is nullable and option correctness is independently tracked by options.is_correct,
-- so the correct fix is ON DELETE SET NULL: deleting the referenced option simply clears the pointer
-- (on a question that, in the delete path, is itself being removed in the same transaction) instead
-- of blocking the delete.

ALTER TABLE questions DROP CONSTRAINT IF EXISTS fk_questions_correct_option;
ALTER TABLE questions ADD  CONSTRAINT fk_questions_correct_option
    FOREIGN KEY (correct_option_id) REFERENCES options (id) ON DELETE SET NULL;
