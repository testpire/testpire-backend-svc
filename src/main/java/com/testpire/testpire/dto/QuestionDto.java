package com.testpire.testpire.dto;


import com.testpire.testpire.enums.Opt;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class QuestionDto {

	public String id;

	private String question;

	private Map<Opt, String> options;

	private List<String> imageUrls;

	private String topic;

	Opt correctAns;

}
