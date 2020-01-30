package com.trifonov.compression;

import lombok.Data;

/**
 * Класс для хранения информации о файле.
 * @author Alexandr Trifonov.
 *
 */
@Data
public class FileInfo {	
	/**
	 * Абсолютное имя файла
	 */
	private String name;
	/**
	 * Размер файла в байтах.
	 */
	private long size;
	
	public FileInfo() {
		
	}
	
	public FileInfo(String name, long size) {
		this.name = name;
		this.size = size;
	}
}
