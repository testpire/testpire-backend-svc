/*
package com.testpire.testpire.util;

import com.cd.onlinetest.enums.DifficultyLevel;
import com.cd.onlinetest.enums.Opt;
import com.cd.onlinetest.mongoDomain.Question;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelReaderUtil {
	public List<Question> extractQuestionFromExcel(String path) throws IOException {
		List<Question> questions = new LinkedList<Question>();
		FileInputStream inputStream = new FileInputStream(new File(path));
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(0);
		if (sheet.getLastRowNum() == 0) {
			workbook.close();
			return questions;
		}

		Row headerRow = sheet.getRow(0);
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			Row currRow = sheet.getRow(i);
			Question ques = extractQuestion(headerRow, currRow);
			questions.add(ques);
		}
		
		workbook.close();

		return questions;
	}

	private Question extractQuestion(Row headerRow, Row currRow) {
		Question ques = new Question();
		Map<Opt, String> options = new LinkedHashMap<>();
		for (int j = 0; j < currRow.getLastCellNum(); j++) {

			switch (headerRow.getCell(j).getStringCellValue()) {
			case "QuestionId":
				ques.setId(getCellValue(currRow.getCell(j)));
				break;
			case "Question_Text":
				ques.setQuestion(getCellValue(currRow.getCell(j)));
				break;
			case "Opt1":
				options.put(Opt.OPT1, getCellValue(currRow.getCell(j)));
				break;
			case "Opt2":
				options.put(Opt.OPT2, getCellValue(currRow.getCell(j)));
				break;
			case "Opt3":
				options.put(Opt.OPT3, getCellValue(currRow.getCell(j)));
				break;
			case "Opt4":
				options.put(Opt.OPT4, getCellValue(currRow.getCell(j)));
				break;
			case "Correct":
				ques.setCorrectAns(Opt.identify(currRow.getCell(j).getStringCellValue()));
				break;
			case "Type":
				ques.setLevel(DifficultyLevel.identify(currRow.getCell(j).getStringCellValue()));
				break;
			}
		}
		ques.setOptions(options);
		return ques;
	}
	
	private static String getCellValue(Cell cell) {
	    switch (cell.getCellTypeEnum()) {
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue());
	        case STRING:
	        	 return String.valueOf(cell.getRichStringCellValue().getString());
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
	            	 return String.valueOf(cell.getDateCellValue());
	            } else {
	            	 return String.valueOf(cell.getNumericCellValue());
	            }
	        case FORMULA:
	        	 return String.valueOf(cell.getCellFormula());
	        case BLANK:
	            return "";
	        default:
	            return "";
	    }
	}


}
*/
