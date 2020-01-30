package com.trifonov.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.trifonov.compression.Compressor;
import com.trifonov.compression.FileInfo;
import com.trifonov.compression.FileProvider;
import com.trifonov.compression.FileReportCreator;
import com.trifonov.compression.ReportCreator;

/**
 * Основной класс для запуска и работы приложения.
 * @author Alexandr Trifonov
 *
 */
public class Main {
	/**
	 * Название файла с количеством сжатых файлов.
	 */
	private final static String COUNT_COMPRESSED_FILE = "count-compressed.txt";
	/**
	 * Название файла со списком файлов, при сжатии которых произошли ошибки.
	 */
	private final static String FAILED_COMPRESSED_FILES = "failed-compressed-files.txt";
	/**
	 * Название файла со списком json-объектов файлов, при сжатии которых произошли ошибки.
	 */
	private final static String FAILED_COMPRESSED_JSON = "failed-compressed-files.json";
	/**
	 * Название файла со списком битых ключей.
	 */
	private final static String FAILED_KEYS = "failed-keys.txt";
	/**
	 * Название файла со списком файлов, при чтении которых произошли ошибки.
	 */
	private final static String FAILED_READ_FILES = "failed-read-files.txt";
	/**
	 * Название файла со списком json-объектов файлов, при чтении которых произошли ошибки.
	 */
	private final static String FAILED_READ_FILES_JSON = "failed-read-files.json";
	/**
	 * Название файла со списком неиспользованных до лимита ключей.
	 */
	private final static String INCOMPLETE_KEYS = "incomplete-keys.txt";
	/**
	 * Название файла со списком оставшихся несжатых файлов.
	 */
	private final static String UNCOMPRESSED_FILES = "uncompressed-files.txt";
	/**
	 * Название файла со списком json-объектов оставшихся несжатых файлов.
	 */
	private final static String UNCOMPRESSED_FILES_JSON = "uncompressed-files.json";
	/**
	 * Название файла со списком использованных до лимита ключей.
	 */
	private final static String WASTE_KEYS = "waste-keys.txt";

	private static final Logger logger = LogManager.getLogger();
	
	public static void main(String[] args) {		
		
		Path sourcePath = FileProvider.getSourcePath();
		System.out.println();
		Path keysPath = FileProvider.getKeysPath();
		System.out.println();
		
		Compressor compressor = new Compressor();
		compressor.compress(sourcePath, keysPath);
		
		Main main = new Main();
		Properties props = main.props();
		if (!props.isEmpty()) {
			AtomicInteger countCompressed = compressor.getCountCompressed();		
			Queue<FileInfo> failedCompressedFiles = compressor.getFailedCompressedFiles();
			Queue<FileInfo> uncompressedFiles = compressor.getUncompressedFiles();
			List<FileInfo> failedReadFilesList = compressor.getFailedReadFilesList();
			Queue<String> failedKeys = compressor.getFailedKeys();
			Queue<String> wasteKeys = compressor.getWasteKeys();
			Queue<String> incompleteKeys = compressor.getIncompleteKeys();			
			
			ReportCreator reportCreator = new FileReportCreator();
			
			reportCreator.createReportNumber(countCompressed.get(),
					props.getProperty("count.compressed_file") != null ? props.getProperty("count.compressed_file"): COUNT_COMPRESSED_FILE);
			reportCreator.createReportFileInfos(failedReadFilesList,
					props.getProperty("failed.read.files") != null ? props.getProperty("failed.read.files") : FAILED_READ_FILES);
			reportCreator.createReportFileInfos(uncompressedFiles, 
					props.getProperty("uncompressed.files.file") != null ? props.getProperty("uncompressed.files.file") : UNCOMPRESSED_FILES);
			reportCreator.createReportFileInfos(failedCompressedFiles, 
					props.getProperty("failed.compressed.files.file") != null ? props.getProperty("failed.compressed.files.file") : FAILED_COMPRESSED_FILES);
			reportCreator.createReportStrings(failedKeys, 
					props.getProperty("failed.keys.file") != null ? props.getProperty("failed.keys.file") : FAILED_KEYS);
			reportCreator.createReportStrings(incompleteKeys,
					props.getProperty("incomplete.keys.file") != null ? props.getProperty("incomplete.keys.file") : INCOMPLETE_KEYS);
			reportCreator.createReportStrings(wasteKeys, 
					props.getProperty("waste.key.files") != null ? props.getProperty("waste.key.files") : WASTE_KEYS);
			reportCreator.createFileInfoJson(failedReadFilesList,
					props.getProperty("failed.read.files.json") != null ? props.getProperty("failed.read.files.json") : FAILED_READ_FILES_JSON);
			reportCreator.createFileInfoJson(uncompressedFiles, 
					props.getProperty("uncompressed.files.json") != null ? props.getProperty("uncompressed.files.json") : UNCOMPRESSED_FILES_JSON);
			reportCreator.createFileInfoJson(failedCompressedFiles, 
					props.getProperty("failed.compressed.files.json") != null ? props.getProperty("failed.compressed.files.json") : FAILED_COMPRESSED_JSON);
		}	
		
	}
	
	/**
	 * Метод для получения properties.
	 * @return Properties
	 */
	public Properties props() {
		Properties props = new Properties();
		
		try(InputStream in = this.getClass().getResourceAsStream("/report.properties")) {
			props.load(in);			
		} catch(IOException e) {
			logger.error("Failed get propeties. IOException. ", e);
		}
		catch (Exception e) {
			logger.error("Failed get propeties. Exception. ", e);
		}
		return props;
	}
}
