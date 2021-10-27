package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResultCollector {

	public static String path = "result_multi";
	public static String path_d = "result_multi_daivd";

	public static String[] preFix = { "util", "nop", "nopF", "paral" };
	public static String[] sufFix = { "he", "our", "seq" };

	public static void main(String args[]) {
		List<String> allFiles = getAllNames(path);
		
		for(String file : allFiles) {
			System.out.println(file);
		}
	}

	public static List<String> getAllNames(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		List<String> files = new ArrayList<>();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
//				System.out.println("File " + listOfFiles[i].getName());
				
				String fileName = listOfFiles[i].getName();
				
				boolean isResult = false;
				for(String pre : preFix) {
					if(fileName.contains(pre))
					{
						isResult = true;
						break;
					}
				}
				
				if(isResult)
					files.add(listOfFiles[i].getName());
			}
		}

		return files;
	}

}
