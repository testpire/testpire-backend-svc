/*
package com.testpire.testpire.service;

import com.cd.onlinetest.config.DbChannel;
import com.cd.onlinetest.dto.QuestionDto;
import com.cd.onlinetest.enums.DifficultyLevel;
import com.cd.onlinetest.mongoDomain.Question;
import com.cd.onlinetest.util.ExcelReaderUtil;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QuestionService {

	@Autowired
	DbChannel dbChannel;

	@Autowired
	ExcelReaderUtil excelReaderUtil;

	public List<Question> getQuestion(String topic, Integer numQues, DifficultyLevel difficultylevel) {
		List<Question> questions = dbChannel.getQuestion(topic, numQues, difficultylevel);
		Collections.shuffle(questions);
		List<Question> questionList = questions.subList(0, Math.min(numQues, questions.size()));
		return questionList;
	}

	// TODO : update question in chunks
	public void updateQuestion(String path) {
		List<Question> questions = null;
		try {
			questions = excelReaderUtil.extractQuestionFromExcel(path);
		} catch (Exception e) {
			log.error("unable to read questions from excel", e);
		}
		dbChannel.saveQuestions(questions);
	}

	public void deleteQuestion(String topic) {
		dbChannel.deleteQuestion(topic);
	}

	public List<QuestionDto> getQuestionByTopicId(String topicId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<QuestionDto> getQuestionByTestId(String testId) {
		// TODO Auto-generated method stub
		return null;
	}

}
*/
