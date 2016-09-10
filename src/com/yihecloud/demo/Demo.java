package com.yihecloud.demo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class Demo {
	public static Map<String, Integer> map = new HashMap<String, Integer>();
	
	public static void main(String[] args) {
		try {
			String val="";
			String path="";
			Scanner scanner = new Scanner(System.in);// 创建输入流扫描器
		    System.out.println("请输入解压文件路径：");// 提示用户输入
		    path = scanner.nextLine();// 获取用户输入的一行文本
		    System.out.println("请输入解压密码：");// 提示用户输入
		    val = scanner.nextLine();// 获取用户输入的一行文本
			unzipDirWithPassword(path, val);
			ValueComparator bvc = new ValueComparator(map);
			TreeMap<String, Integer> tempMap = new TreeMap<String, Integer>(bvc);
			tempMap.putAll(map);
			Integer num = 0;
			for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
				if (num == 5) {
					break;
				}
				System.err.println( entry.getKey()+ "：" + entry.getValue());
				num++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void unzipDirWithPassword(final String sourceZipFile, final String password) {
		RandomAccessFile randomAccessFile = null;
		ISevenZipInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(sourceZipFile, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			// Getting simple interface of the archive inArchive
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			for (final ISimpleInArchiveItem item : simpleInArchive
					.getArchiveItems()) {
				final int[] hash = new int[] { 0 };
				if (!item.isFolder()) {
					ExtractOperationResult result;
					result = item.extractSlow(new ISequentialOutStream() {
						public int write(final byte[] data)
								throws SevenZipException {
							try {
								String str = new String(data, "utf-8");
								String[] strArr = str.split(",");

								for (int i = 0; i < strArr.length; i++) {
									Integer temp = map.get(strArr[i]);
									if (temp != null) {
										map.put(strArr[i], temp + 1);
									} else {
										map.put(strArr[i], 1);
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							hash[0] |= Arrays.hashCode(data);
							return data.length; // Return amount of proceed data
						}
					}, password); // / password.
					if (result == ExtractOperationResult.OK) {
						System.out.println(String.format("%9X | %s", hash[0],
								item.getPath()));
					} else {
						System.err.println("Error extracting item: " + result);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e);
					e.printStackTrace();
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					System.err.println("Error closing file: " + e);
					e.printStackTrace();
				}
			}
		}
	}
}

class ValueComparator implements Comparator<String> {

	Map<String, Integer> base = new HashMap<String, Integer>();

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}