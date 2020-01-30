package com.trifonov.compression;

import java.util.Collection;

/**
 * Интерфейс для создания отчетов по результатам сжатия файлов.
 * @author Alexandr Trifonov
 *
 */
public interface ReportCreator {
	/**
	 * Создает отчет для очереди из объектов типа FileInfo.
	 * @param collection очередь объектов FileInfo
	 * @param reportName имя для отчета
	 */
	void createReportFileInfos(Collection<FileInfo> fileInfoCollection, String reportName);
	
	/**
	 * Создает отчет для очереди из объектов типа String.
	 * @param collection очередь объектов String
	 * @param reportName имя для отчета
	 */
	void createReportStrings(Collection<String> stringCollection, String reportName);
	
	/**
	 * Создает отчет для числа.
	 * @param number число
	 * @param reportName имя для отчета
	 */
	void createReportNumber(int number, String reportName);
	
	/**
	 * Создает объекты json из полученной коллекции с элементами типа FileInfo.
	 * @param files коллекция с элементами типа FileInfo
	 * @param fileName
	 */
	void createFileInfoJson(Collection<FileInfo> files, String fileName);
}
