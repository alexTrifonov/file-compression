package com.trifonov.compression;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * Класс для обхода директории с файлами для сжатия и получения списка доступных файлов и файлов, которые не удалось прочитать. 
 * @author Alexandr Trifonov
 *
 */
@Data
@RequiredArgsConstructor
public class ImageFileVisitor implements FileVisitor<Path> {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * Список доступных файлов для сжатия.
	 */
	@NonNull
	private List<FileInfo> list;
	/**
	 * Список файлов, которые не удалось прочитать при проходе по директории.
	 */
	@NonNull
	private List<FileInfo> failedList;
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		list.add(new FileInfo(file.toString(), Files.size(file)));
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		logger.info("failed visit file = {}", file);
		failedList.add(new FileInfo(file.toString(), 0));
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}
}
