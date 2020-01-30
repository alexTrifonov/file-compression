package com.trifonov.compression;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Класс для получения директории с файлами для сжатия и файла с ключами
 * @author Alexandr Trifonov.
 *
 */
public class FileProvider {
	private static Scanner scanner = new Scanner(System.in);
	
	/**
	 * Метод для получения в консоли абсолютного пути к директории с файлами для сжатия либо абсолютного имени json-файла с файлами для сжатия. 
	 * @return Path с директорией/файлом.
	 */
	public static Path getSourcePath() {
		
		System.out.println("For a file list in a text file enter absolute file name.");
		System.out.println("For a directory with files enter absolute directory path.");
		System.out.println("Enter files list source:");
		
		Path sourcePath = null;
		
		int i = 0;
		String source = "";
		
		
		
		while(i < 3) {
			source = scanner.nextLine();
			sourcePath = Paths.get(source);
			if (Files.exists(sourcePath)) {
				break;				
			}
			i++;
			if (i<3) {
				System.out.println("Object not found. Enter correct file name or directory path:");
			} else {
				scanner.close();
				System.out.println("Object not found. Don't be stupid.");
				System.out.println("Program is completed");
				System.exit(0);
			}
			
		}		
		return sourcePath;
	}
	
	/**
	 * Метод для получения абсолютного имени файла с ключами через консоль.
	 * @return Path файла с ключами
	 */
	public static Path getKeysPath() {
		Path keysPath = null;
		System.out.println("Enter absolute file name with keys:");
		int i = 0;		
		String keysFile = "";
		while (i < 3) {
			keysFile = scanner.nextLine();
			keysPath = Paths.get(keysFile);
			if (Files.exists(keysPath)) {
				break;
			} 
			i++;
			if (i < 3) {
				System.out.println("Object not found. Enter correct file name with keys:");
			} else {
				scanner.close();
				System.out.println("File not found. Don't be stupid.");
				System.out.println("Program is completed");
				System.exit(0);
				
			}
		}
		return keysPath;
	}
}
