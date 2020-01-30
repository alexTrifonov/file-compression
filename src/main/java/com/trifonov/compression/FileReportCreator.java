package com.trifonov.compression;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Класс для записи отчетов в файлы.
 * @author Alexandr Trifonov.
 *
 */
public class FileReportCreator implements ReportCreator {
	/**
	 * Имя папки с отчетами
	 */
	private final static String REPORT_DIR = "report-tinypng";
	
	private static final Logger logger = LogManager.getLogger();
	private final Clock clock = Clock.tickSeconds(ZoneId.systemDefault());
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
	private final String lineSeparator = System.lineSeparator();
	private final ObjectMapper objMapper = new ObjectMapper();
	
	/**
	 * Path с абсолютным путем для размещения создаваемых отчетов.
	 */
	private Path reportPath;
	
	public FileReportCreator() {
		Path currentPathAbs = Paths.get("").toAbsolutePath();
		reportPath = currentPathAbs.resolve(Paths.get(REPORT_DIR));
		if (!Files.exists(reportPath)) {
			try {
				Files.createDirectory(reportPath);
			} catch (IOException e) {
				logger.error("Error create reportPath ", e);
				reportPath = currentPathAbs;
			} catch (Exception e) {
				logger.error("Error create reportPath ", e);
				reportPath = currentPathAbs;
			}
		}
		logger.info("Report dir = {}", reportPath); 		
		
	}
	
	
	
	
	@Override
	public void createReportFileInfos(Collection<FileInfo> fileInfoCollection, String reportName) {
		if (!fileInfoCollection.isEmpty()) {
			String report =   fileInfoCollection.stream().map(FileInfo::getName).map(x -> x + lineSeparator).reduce("", String::concat).trim();
			writeReport(reportName, report);
		}	

	}

	@Override
	public void createReportStrings(Collection<String> stringCollection, String reportName) {
		if (!stringCollection.isEmpty()) {		
			String report =   stringCollection.stream().map(x -> x + lineSeparator).reduce("", String::concat).trim();
			writeReport(reportName, report);
		}

	}

	@Override
	public void createReportNumber(int number, String reportName) {
		String numberStr = Integer.toString(number);
		writeReport(reportName, numberStr);

	}
	
	private void writeReport(String fileName, String report) {		
		LocalDateTime date = LocalDateTime.now(clock);
		Path path = reportPath.resolve(Paths.get(String.format("%s-%s", date.format(dateFormatter), fileName)));
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {				
			writer.write(report);		
		} catch (IOException e) {
			logger.error("Writing IOException. ", e);		
		} catch (Exception e) {
			logger.error("Writing exception. ", e);		
		}		
	}
	
	@Override
	public void createFileInfoJson(Collection<FileInfo> files, String fileName) {
		if (!files.isEmpty()) {
			LocalDateTime date = LocalDateTime.now(clock);
			Path path = reportPath.resolve(Paths.get(String.format("%s-%s", date.format(dateFormatter), fileName)));			
			try {
				objMapper.writeValue(path.toFile(), files);
			} catch (JsonGenerationException e) {
				logger.error("JsonGenerationException", e);	
			} catch (JsonMappingException e) {
				logger.error("JsonMappingException", e);	
			} catch (IOException e) {
				logger.error("JSON writing IOException", e);	
			}
		}
	}
}
