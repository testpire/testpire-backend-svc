/*
package com.testpire.testpire.Controller;

import com.cd.onlinetest.dto.TestDto;
import com.cd.onlinetest.enums.CDConstants;
import com.cd.onlinetest.payload.response.MessageResponse;
import com.cd.onlinetest.request.CreateTestRequest;
import com.cd.onlinetest.service.TestService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/test")
public class TestController {

	@Autowired
	TestService testService;

	@GetMapping("/all")
	@PreAuthorize(CDConstants.TEACHER_OR_ADMIN_ROLE)
	public ResponseEntity<List<TestDto>> getAllTests() {
		return ResponseEntity.status(HttpStatus.OK).body(testService.getAllTests());
	}

	@PostMapping("/create")
	@PreAuthorize(CDConstants.ADMIN_ROLE)
	public ResponseEntity<MessageResponse> createTest(@RequestBody CreateTestRequest createTestRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(testService.createTest(createTestRequest));
	}

}
*/
