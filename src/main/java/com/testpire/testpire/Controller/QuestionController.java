/*
package com.testpire.testpire.Controller;

import com.cd.onlinetest.dto.QuestionDto;
import com.cd.onlinetest.enums.DifficultyLevel;
import com.cd.onlinetest.mongoDomain.Question;
import com.cd.onlinetest.service.QuestionService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/question")
public class QuestionController {

	@Autowired
	QuestionService questionService;
	
	@GetMapping(value = "/test")
	public ResponseEntity<String> test() {
		log.info("getQuestion subject method called :");
		return ResponseEntity.status(HttpStatus.OK).body("ok");
	}
	
	@GetMapping(value = "/byTopic")
	public ResponseEntity<List<QuestionDto>> getQuestionByTopicId(@RequestParam(required =false) String topicId) {
		log.info("getQuestion subject method called :");
		return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionByTopicId(topicId));
	}
	
	@GetMapping(value = "/byTestId")
	public ResponseEntity<List<QuestionDto>> getQuestionByTestId(@RequestParam(required =false) String testId) {
		log.info("getQuestion subject method called :");
		return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionByTestId(testId));
	}

	@GetMapping(value = "/getQuestion")
	public ResponseEntity<List<Question>> getQuestion(@RequestParam(required =false) String topic,
			@RequestParam(required = true) Integer numQues, @RequestParam DifficultyLevel difficultylevel) {
		log.info("getQuestion subject method called :");
		return ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestion(topic, numQues, difficultylevel));
	}

	@PostMapping(value = "/updateQuestion")
	public ResponseEntity<String> updateQuestion(@RequestParam String pathOfExcel) {
		log.info("updateQuestion subject method called :");
		questionService.updateQuestion(pathOfExcel);
		return ResponseEntity.status(200).body("updated Successfully");
	}
	
	@DeleteMapping(value = "/deleteQuestion")
	public ResponseEntity<String> deleteQuestion(@RequestParam String topic) {
		log.info("deleteQuestion subject method called :");
		questionService.deleteQuestion(topic);
		return ResponseEntity.status(200).body("delete Successfully");
	}

}
*/
