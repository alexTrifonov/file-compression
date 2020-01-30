package com.trifonov.compression;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinify.AccountException;
import com.tinify.ClientException;
import com.tinify.ConnectionException;
import com.tinify.ServerException;
import com.tinify.Source;
import com.tinify.Tinify;

/**
 * Класс для сжатия файлов. Непосредственно для сжатия используется библиотека Tinify.
 * @author Alexandr Trifonov.
 *
 */
public class Compressor {
	/**
	 * Количество потоков для сжатия файлов.
	 */
	private final static int THREAD_COUNT = 10;
	private final Logger logger = LogManager.getLogger();
	
	/**
	 * Количество сжатых файлов.
	 */
	private AtomicInteger countCompressed;
	/**
	 * Очередь с файлами для сжатия.
	 */
	private Queue<FileInfo> files;
	/**
	 * Очередь с файлами, которые не удалось сжать.
	 */
	private Queue<FileInfo> failedCompressedFiles;
	/**
	 * Очередь с несжатыми файлами.
	 */
	private Queue<FileInfo> uncompressedFiles;
	/**
	 * Список с файлами к которым не был получен доступ.
	 */
	private List<FileInfo> failedReadFilesList;
	/**
	 * Очередь с битыми ключами.
	 */
	private Queue<String> failedKeys;
	/**
	 * Очередь с израсходованными ключами.
	 */
	private Queue<String> wasteKeys;
	/**
	 * Очередь с неиспользованными до лимита ключами.
	 */
	private Queue<String> incompleteKeys;		
	
	/**
	 * Метод для сжатия файлов. В качестве входного параметра принимает абсолютный пути директории с файлами для первоначального процесса сжатия. Если после предыдущего запуска приложения остались несжатые файлы в 
	 * виде json-объектов, то метод также принимает в качестве входного параметра абсолютное имя файла с json-объектами.
	 * Сжатие файлов происходит в несколько потоков. Каждый поток использует один ключ для сжатия нескольких файлов. Один ключ позволяет сжать 500 файлов.
	 * Перед началом сжатия происходит сортировка файлов по размеру и сжатие начинается с файлов с максимальным размером.
	 * @param sourcePath Директория с файлами для сжатия или файл с json-объектами файлов для сжатия. 
	 * @param keysPath Абсолютное имя файла с ключами.
	 */
	public void compress(Path sourcePath, Path keysPath) {		
		List<FileInfo> initFilesList = new LinkedList<>();
		failedReadFilesList = new LinkedList<>();
		
		
		fillFilesList(sourcePath, initFilesList, failedReadFilesList);
		initFilesList = initFilesList.stream().sorted(Comparator.comparing(FileInfo::getSize).reversed()).collect(Collectors.toList());
		files = new ConcurrentLinkedQueue<>(initFilesList);
		initFilesList = null;
		
		List<String> keys = getKeys(keysPath);
		countCompressed = new AtomicInteger(0);
		failedCompressedFiles = new ConcurrentLinkedQueue<>();
		uncompressedFiles = new ConcurrentLinkedQueue<>();		
		failedKeys = new ConcurrentLinkedQueue<>();
		wasteKeys = new ConcurrentLinkedQueue<>();
		incompleteKeys = new ConcurrentLinkedQueue<>();		
		
		ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
		
		Iterator<String> keyIterator = keys.iterator();	
		while(keyIterator.hasNext()) {
			String key = keyIterator.next();
			pool.submit(() -> {
				Tinify.setKey(key);
				boolean validKeyAndHasFile = true;
				while(validKeyAndHasFile) {
					if (!files.isEmpty()) {				
						FileInfo file = files.poll();
						try {
							Source  source = Tinify.fromFile(file.getName());
							source.toFile(file.getName());
							countCompressed.incrementAndGet();
							logger.info("Compressed file = {}, size = {}", file.getName(), file.getSize());				
						} catch (AccountException e) {
							wasteKeys.add(key);
							uncompressedFiles.add(file);				
							validKeyAndHasFile = false;
							logger.error("AccountException, message = {}, key = {}, file = {}, size = {}", e.getMessage(), key, file.getName(), file.getSize(), e);
						} catch (ClientException e) {
							failedCompressedFiles.add(file);
							logger.error("ClientException, message = {}, key = {}, file = {}, size = {}", e.getMessage(), key, file.getName(), file.getSize(), e);
						} catch (ServerException e) {
							uncompressedFiles.add(file);
							logger.error("ServerException, message = {}, key = {}, file = {}, size = {}", e.getMessage(), key, file.getName(), file.getSize(), e);
						} catch (ConnectionException e) {
							uncompressedFiles.add(file);
							logger.error("ConnectionException, message = {}, key = {}, file = {}, size = {}", e.getMessage(), key, file.getName(), file.getSize(), e);
						} catch (java.lang.Exception e) {
							uncompressedFiles.add(file);
							logger.error("java.lang.Exception, message = {}, key = {}, file = {}, size = {}", e.getMessage(), key, file.getName(), file.getSize(), e);
						}
					} else {
						incompleteKeys.add(key);
						validKeyAndHasFile = false;
						logger.info("Files are finished. key = {}", key);
					}	
				}
			});		
			
		}
		
		logger.info("Compression is finished. Compressions count = " + countCompressed);
		
		pool.shutdown();
		while(!pool.isTerminated()) {}
		
		if (!files.isEmpty()) {
			uncompressedFiles.addAll(files);
		}
				
	}

	public List<FileInfo> getFailedReadFilesList() {
		return failedReadFilesList;
	}

	public AtomicInteger getCountCompressed() {
		return countCompressed;
	}

	public Queue<FileInfo> getFiles() {
		return files;
	}

	public Queue<FileInfo> getFailedCompressedFiles() {
		return failedCompressedFiles;
	}

	public Queue<FileInfo> getUncompressedFiles() {
		return uncompressedFiles;
	}

	public Queue<String> getFailedKeys() {
		return failedKeys;
	}

	public Queue<String> getWasteKeys() {
		return wasteKeys;
	}

	public Queue<String> getIncompleteKeys() {
		return incompleteKeys;
	}

	/**
	 * Метод для наполнения списка json-объектов файлов для сжатия и списка json-объектов файлов, к которым не удалось получить доступ. 
	 * @param sourcePath Директория с файлами либо файл со списком json-объектов.
	 * @param initFilesList Список json-объектов файлов для сжатия.
	 * @param failedReadFilesList Список json-объектов файлов, к которым не удалось получить доступ. 
	 */
	private void fillFilesList(Path sourcePath, List<FileInfo> initFilesList, List<FileInfo> failedReadFilesList) {				
		if (Files.isDirectory(sourcePath)) {
			ImageFileVisitor visitor = new ImageFileVisitor(initFilesList, failedReadFilesList);
			try {
				Files.walkFileTree(sourcePath, visitor);				
			} catch (IOException e) {
				logger.error("Failed get list all files for compressing. IOException. ", e);
			} catch (Exception e) {
				logger.error("Failed get list all files for compressing. IOException. ", e);
			}		
			
			if (initFilesList.isEmpty()) {
				logger.info("File list for compressing is empty");
			}
		} else {
			ObjectMapper objMapper = new ObjectMapper();
			try {
				List<FileInfo> files = objMapper.readValue(sourcePath.toFile(), new TypeReference<List<FileInfo>>() {});
				initFilesList.addAll(files);
			} catch (JsonParseException e) {
				logger.error("JsonParseException. Failed getting initFilesList", e);
			} catch (JsonMappingException e) {
				logger.error("JsonMappingException. Failed getting initFilesList", e);
			} catch (IOException e) {
				logger.error("IOException. Failed getting initFilesList", e);
			}
		}
	}
	
	/**
	 * Метод для получения списка ключей.
	 * @param keysPath Файл с ключами.
	 * @return
	 */
	private List<String> getKeys(Path keysPath) {
		List<String> keys = new LinkedList<>();
		try {
			keys = new LinkedList<>(Files.readAllLines(keysPath, Charset.forName("UTF-8")));
			keys.removeIf(str -> str.isEmpty());
			logger.info("keys: {}", keys);
		} catch (IOException e) {
			logger.error("Failed read keys list. IOException. ", e);			
		} catch (Exception e) {
			logger.error("Failed read keys list. Exception. ", e);			
		}		
		return keys;
	}
	
	
	
	
}
