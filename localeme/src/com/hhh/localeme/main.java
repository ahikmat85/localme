package com.hhh.localeme;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

import jxl.Cell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class main {

	//
	public static HashMap<String, ArrayList<Infox>> list = new HashMap<String, ArrayList<Infox>>();//
	public static int start_column_index = 1;
	public static int start_row_index = 1;
	public static String[] filters = new String[] { ".java", ".xml", ".mxml",
			".property" };

	public static class Infox {
		public Infox() {

		}
		public String path;
		public String text;
		public int line_number;
	}
	//

	/**
	 * @param args
	 * args[0] - action type : extract,translate
	 * * args[1] - path to excel file : /Users.ahikmat/Documents/translation.xls
	 * args[2] - path to source cdoe : /Users/ahikmat/Documents/3hs/workspace/myproject
	 */
	public static void main(String[] args) {

		// file
		String excel_file = args[1];
		String path = args[2];
		if(args[0].equals("translate"))
			translateProject(excel_file);
			else
			extractTexts(path, excel_file);
		//
		System.out.println("Completed!");
	}

	public static void translateProject(String excel_file)
	{
		
		try {
			Workbook workbook = Workbook
					.getWorkbook(new File(excel_file));
			Sheet sheet = workbook.getSheet(0);
			HashMap<String,HashMap<Integer,ArrayList<String>>> file_list = new HashMap<String, HashMap<Integer,ArrayList<String>>>();
			HashMap<String,String> replacement = new HashMap<String, String>();
			//
			for(int row=start_row_index;row<sheet.getRows();row++)
			{
				Cell cell = sheet.getCell(start_column_index, row);
				String text = cell.getContents();
				String translation = sheet.getCell(start_column_index-1, row).getContents();//translation
				replacement.put(text, translation);
				//path based list
				for(int column = start_column_index+1;column<sheet.getColumns();column+=2)
				{
				String path = sheet.getCell(column, row).getContents();
				String coord = sheet.getCell(column+1, row).getContents();
				//
				if(path.length()==0) break;					
				//
				Integer line_number = Integer.parseInt(coord);
				//init
				if(file_list.get(path)==null)
					file_list.put(path, new HashMap<Integer, ArrayList<String>>());
				if(file_list.get(path).get(line_number)==null)
				{
					file_list.get(path).put(line_number, new ArrayList<String>());
				}
				//add values
				file_list.get(path).get(line_number).add(text);
				
				}
			
				//Write to files
				for(String path:file_list.keySet())
				{
					HashMap<Integer,ArrayList<String>> lines = file_list.get(path);
					File file = new File(path);
					File fileTemp = new File("temp.txt");
					file.renameTo(fileTemp);
					BufferedWriter bw;
					bw = new BufferedWriter(new FileWriter(file));
					BufferedReader br;
					br = new BufferedReader(new FileReader(fileTemp));
					String line;
					int line_number=0;

					while ((line = br.readLine()) != null) {
						ArrayList<String> texts =lines.get(line_number); 
						if(texts!=null)
						{
							Collections.sort(texts,new MyComparator());
							for(String old_text:texts)
							{
								System.out.println("line:"+line_number+" text:"+old_text);
								line =  line.replace(old_text, replacement.get(old_text));
							}
						}
						bw.write(line);
						bw.newLine();
						line_number++;
					}
					br.close();
					bw.close();
					fileTemp.delete();
					
				}
			}
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static class MyComparator implements java.util.Comparator<String> {


	    public MyComparator() {
	        super();
	    }

	    public int compare(String o1, String o2) {
	    	if (o1.length() > o2.length()) {
	            return -1;
	         } else if (o1.length() < o2.length()) {
	            return 1;
	         }
	    	return o1.compareTo(o2);

	    	}
	}
	public static void extractTexts(String path, String excel_file)
	{
		//recurs through folders
		checkFolder(path);
		// excel part
		try
		{
			WritableWorkbook workbook = Workbook.createWorkbook(new File(excel_file));
			WritableSheet sheet =workbook.createSheet("sheet", 0);
			//
			int row=start_row_index;
			for (ArrayList<Infox> infs : list.values()) {
				int column=start_column_index;
				for(Infox inf:infs)//get all cases
				{
					//text
					if(column==start_column_index)
					{
						Label lblText = new Label(column, row, inf.text);
						sheet.addCell(lblText);
					}
					//path
					Label lblPath = new Label(column+1, row, inf.path);
					sheet.addCell(lblPath);
					//line number, start index, end index
					Label lblCoord = new Label(column+2, row, String.valueOf(inf.line_number));
					sheet.addCell(lblCoord);
					column+=2;
				}
				row++;
			}
			//
			workbook.write();
			workbook.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	public static void checkFolder(String path) {
		File folder = new File(path);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				checkFolder(file.getAbsolutePath());
			} else {
				// file
				for (String filter : filters)
					if (file.getName().endsWith(filter)) {
						// process
						System.out.println("Processing:"+file.getAbsolutePath());
						try {
							BufferedReader br;
							br = new BufferedReader(new FileReader(file));
							String line;
							int line_number=0;

							while ((line = br.readLine()) != null) {
								// process the line.
								int start_index = -1, end_index = -1;
								//
								for (int i = 0; i < line.length() - 1; i++) {
									char ch = line.charAt(i);
									if (ch == '\"') {
										if (line.charAt(i - 1) != '\\')// jump
																		// to
																		// ignore
																		// singles
										{
											if (start_index >= 0) // closing
											{
												end_index = i;
												// add
												String text = line.substring(
														start_index + 1,
														end_index);
												if (text.length() > 0) {
													
													Pattern p = Pattern
															.compile("[\u0000-\u007F]*");
													Matcher m = p.matcher(text);

													if (!m.matches()) {
														Infox inf = new Infox();
														inf.path = file.getAbsolutePath();
														inf.line_number = line_number;
														inf.text = text;
														if(list.get(text)==null)
															list.put(text, new ArrayList<main.Infox>());
														list.get(text).add(inf);
													}
												}
												start_index = -1;
											} else
												start_index = i;
										}
									}
								}
								line_number++;
							}
							br.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
			}
		}
	}

}
