package com.testpire.testpire.dto;

import lombok.Data;

@Data
public class TestDto {

	public String id;

	private String name;

	private String duration;

	public TestDto(String id, String name, String duration) {
		super();
		this.id = id;
		this.name = name;
		this.duration = duration;
	}

	
}
