/*
package com.testpire.testpire.service;

import com.cd.onlinetest.dto.TestDto;
import com.cd.onlinetest.mongoDomain.Test;
import com.cd.onlinetest.payload.response.MessageResponse;
import com.cd.onlinetest.repository.TestRepository;
import com.cd.onlinetest.request.CreateTestRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TestService {

	@Autowired
	TestRepository testRepository;

	public List<TestDto> getAllTests() {
		List<TestDto> testDtos = new ArrayList<>();
		List<Test> dbTests = testRepository.findAll();
		dbTests.forEach(dbTest -> {
			TestDto tdto = new TestDto(dbTest.getId(), dbTest.getName(), dbTest.getDuration());
			testDtos.add(tdto);
		});
		return testDtos;
	}

	// TODO : match if questionId's exists in db
	public MessageResponse createTest(CreateTestRequest createTestRequest) {
		Test dbtest = new Test();
		dbtest.setQuestionIds(createTestRequest.getQuestionIds());
		dbtest.setName(createTestRequest.getDuration());
		dbtest.setDuration(createTestRequest.getDuration());
		testRepository.save(dbtest);
		return new MessageResponse(HttpStatus.OK, "test created successfully");
	}

}
*/
