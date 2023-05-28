package Preprocessing;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




public class Preprocessing {
	
	static HashMap<String,Integer> countryISO = new HashMap<String, Integer>();
	static HashMap<String, Integer> countryRegexISO = new HashMap<String, Integer>();
	
	static final String IN_PATH = "C:\\Users\\nikos\\Desktop\\OldData\\";
	static final String OUT_PATH = "C:\\Users\\nikos\\Desktop\\FixedData\\";
	
	public static void main(String[] args)
	{
		try
		{
			setUpCountries(); //Initial setup for the countries table
			readRegexNames(); //Read list of country name regexes	
			setUpPopulationByAgeBig();
			setUpIncome();
			setUpPopulationByRange();
			
			String[] correctCSVList = new String[] {"age_specific_fertility_rates.csv","birth_death_growth_rates.csv",
													"country_names_area.csv", "mortality_life_expectancy.csv", 
													"midyear_population.csv"};
			
			for (String i : correctCSVList)
			{
				setUpGeneralCSV(i);
			} 
			System.out.println("Done.");  

		}
		catch (Exception e)
		{
			System.out.println(e);
		}	

		
	}
	
	static void setUpGeneralCSV(String path) throws Exception
	{
		ArrayList<String[]> data = getRows(IN_PATH + path);
		
		File writeFile = new File(OUT_PATH+"ETL_" + path);
		if (writeFile.exists()) { writeFile.delete(); }
		writeFile.createNewFile();
		
		FileWriter writer = new FileWriter(writeFile);
		boolean wroteFirstLine = false;
		
		for (String[] line : data)
		{
			String output = "";
			if (wroteFirstLine)
			{
				String ISO = getISO(line[1]);
				if (ISO.equals("NULL")) { continue; }
				
				output += ISO + ",";
			}
			else
			{
				output += "iso_code,";
				wroteFirstLine = true;
			}
			
			
			
			
			for (String i : line)
			{
				output += i + ",";
			}
			writer.write(output.substring(0, output.length()-1) +"\n");
		}
		writer.close();

	}
	
	static void setUpPopulationByAgeBig() throws Exception
	{
		Scanner reader = new Scanner(new File(IN_PATH + "midyear_population_age_country_code.csv"));
		File writeFile = new File(OUT_PATH + "ETL_midyear_big_population.csv");
		if (writeFile.exists()) { writeFile.delete(); }
		writeFile.createNewFile();
		
		FileWriter writer = new FileWriter(writeFile);
		boolean wroteFirstLine = false;
		HashMap<String, Integer> pairs = new HashMap<String, Integer>();
		
		while (reader.hasNextLine())
		{
			String[] line = reader.nextLine().split(",");
			String output = "";
			if (wroteFirstLine)
			{
				
				String ISO = getISO(line[1]);
				if (ISO.equals("NULL")) { continue; }
				
				String tag = ISO + "-" + line[2] + "-" + line[3];
				if (pairs.containsKey(tag)) { continue; }
				else { pairs.put(tag, 1); }
				
				
				
				output += ISO + ",";		
			}
			else
			{
				
				output += "country_code,country_name,year,sex,";
				for (int i = 0; i < 101; i++)
				{
					output += "population_age_" + i + ",";
				}
				output = output.substring(0, output.length()-1) + "\n";
				wroteFirstLine = true;
				writer.write(output);
				continue;
			}
			
			for (int i = 1; i < line.length; i++)
			{
				if (i == 4 || i >= 106) continue;
				output += line[i] + ",";
			}
			writer.write(output.substring(0, output.length()-1) + "\n");
			
		}
		reader.close();
		writer.close();
	}
	
	
	
	static void setUpPopulationByRange() throws Exception
	{
		ArrayList<String[]> data = getRows(IN_PATH + "midyear_population_5yr_age_sex.csv");
		
		File writeFile = new File(OUT_PATH+"ETL_midyear_population_5yr_age_sex.csv");
		if (writeFile.exists()) { writeFile.delete(); }
		writeFile.createNewFile();
		
		FileWriter writer = new FileWriter(writeFile);
		boolean wroteFirstLine = false;
		
		for (String[] line : data)
		{
			
			String output = "";
			int startValue = -1;
			int endValue = -1;
			
			if (wroteFirstLine)
			{
				String ISO = getISO(line[1]);
				if (ISO.equals("NULL")) { continue; }
				
				output += ISO + ",";
				
				
				if (line[3].equals("*")) 
				{ 
					startValue = 0;
					endValue = 999;
				}
				else //Case "A"
				{
					if (line[5].equals("+"))
					{
						startValue = Integer.parseInt(line[4]);
						endValue = 999;
					}
					else
					{
						startValue = Integer.parseInt(line[4]);
						endValue = Integer.parseInt(line[6]);
					}
				}
			}
			else
			{
				output += "iso_code,country_code,country_name,year,starting_age,ending_age,midyear_population,"
						+ "midyear_population_male,midyear_population_female\n";
				wroteFirstLine = true;
				writer.write(output);
				continue;
			}
			

			
			
			
			for (int i = 0; i < line.length; i++)
			{

				if (i == 3 || i == 5) { continue; }
				if (i == 4) { output += startValue + ","; continue;}
				if (i == 6) { output += endValue + ","; continue; }

				output += line[i] + ",";
			}
			writer.write(output.substring(0, output.length()-1) +"\n");
		}
		writer.close();

	}
	
	
	static void setUpIncome() throws Exception
	{
		HashMap<String, String[]> incomeData = getXLSXRows(IN_PATH + "Income by Country.xlsx");
		
		File writeFile = new File(OUT_PATH + "ETL_income.csv");
		if (writeFile.exists()) { writeFile.delete(); }
		writeFile.createNewFile();
		
		FileWriter writer = new FileWriter(writeFile);
		writer.write("ISO,country,year,income_index,labour_share_GDP,gross_fixed_capital_formation,GDP_total,"
				    + "GDP_per_capita,GNI_per_capita,GNI_estimate_male,GNI_estimate_female,domestic_credit\n");
		
		for (String key : incomeData.keySet())
		{

			String output = "\"";
			String[] spl = key.split("-");
			for (int i = 0; i < spl.length-1; i++) { output += spl[i] + "-"; }
			
			String countryName = output.substring(1, output.length()-1);
			String ISO = getISO(countryName);
			
			if (ISO.equals("NULL")) { continue; }
			
			
			output = ISO + "," + output.substring(0, output.length()-1) + "\"," + spl[spl.length-1] + ",";

				
			for (String i : incomeData.get(key))
			{
				if (i == null) { output += "NULL,"; continue; }
				output += i + ",";
			}
			
			writer.write(output.substring(0, output.length()-1) + "\n");
		}
		writer.close();
	}
	
	static HashMap<String, String[]> getXLSXRows(String path) throws Exception
	{
		HashMap<String, String[]> etlData = new HashMap<String, String[]>();
		FileInputStream input = new FileInputStream(new File(path));
		Workbook wb = new XSSFWorkbook(input);
		
		
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++)
		{
			Sheet sheet = wb.getSheetAt(sheetIndex);
			
			ArrayList<String> columnTags = new ArrayList<String>();
			boolean hasReadFirstRow = false;
			for (Row row : sheet)
			{
				ArrayList<String> rowData = new ArrayList<String>();
				
				for (Cell cell : row)
				{
					if (!hasReadFirstRow)
					{
						if (columnTags.size() == 0) { columnTags.add("Country"); continue; }
	
						try { columnTags.add((int)Double.parseDouble(cell.toString()) + ""); } 
						catch (Exception e) {  }
	
						continue;		
					}
					rowData.add(cell.toString());
					
				}
				
				if (!hasReadFirstRow) { hasReadFirstRow = true; continue; }
				
				String countryName = "";
				for (int i = 0; i < rowData.size(); i++)
				{
					if (i==0) { countryName = rowData.get(i); continue; }
					if (i >= columnTags.size()) { break; }
					
					String key = countryName + "-" + columnTags.get(i); //Key: A combination oof country-year (ex. Greece-1960)
					
					if (etlData.containsKey(key))
					{
						if (isNumeric(rowData.get(i)))
						{
							etlData.get(key)[sheetIndex] = rowData.get(i);
						}			
					}
					else
					{
						String[] _temp = new String[wb.getNumberOfSheets()];
						if (isNumeric(rowData.get(i)))
						{
							_temp[sheetIndex] = rowData.get(i);
						}	
						etlData.put(key, _temp);
					}
				}
	
			}
		}
		
		return etlData;
		
	}
	
	//Read the included regex file for country regex names
	static void readRegexNames() throws Exception
	{
		Scanner scanner = new Scanner(new File("country_map.txt"));
		
		while (scanner.hasNextLine())
		{
			String[] spl = scanner.nextLine().split(";");
			countryRegexISO.put(spl[0], Integer.parseInt(spl[1]));
		}
	}
	
	
	//ELP for countries.csv
	static void setUpCountries() throws Exception
	{
		
		ArrayList<String[]> data = getRows(IN_PATH + "countries.csv");
		
		File writeFile = new File(OUT_PATH + "ETL_countries.csv");
		if (writeFile.exists()) { writeFile.delete(); }
		writeFile.createNewFile();
		
		FileWriter writer = new FileWriter(writeFile);
		
		int[] numericFields = new int[] {2, 10, 11, 13, 15, 22, 23};
		int[] booleanFields = new int[] {19, 20, 21};
		boolean hasWrittenFirstLine = false;
		for (String[] line : data)
		{
			if (!hasWrittenFirstLine)
			{
				hasWrittenFirstLine = true;
				continue;		
			}
			
			for (int i : numericFields)
			{
				if (!isNumeric(line[i])) { line[i] = "NULL"; }
			}
			
			for (int i : booleanFields)
			{
				if (line[i].toLowerCase().strip().equals("true")) { line[i] = "1"; }
				else if (line[i].equals("")) { line[i] = "0"; }
				else { line[i] = "NULL"; }
			}
			
			if (!line[18].equals("Developing") && !line[18].equals("Developed")) { line[18] = "NULL"; }
			
			String output = "";
			for (String i : line)
			{
				output += i + ",";
			}
			writer.write(output.substring(0, output.length()-1) + "\n");
			countryISO.put(line[4], Integer.parseInt(line[2]));
		}
		writer.close();
	}
	
	
	
	//Convert CSV to a List of string arrays
	static ArrayList<String[]> getRows(String path)
	{
		try
		{
			Scanner reader = new Scanner(new File(path));
			ArrayList<String[]> data = new ArrayList<String[]>();
			boolean skippedFirstLine = false;
	
			while (reader.hasNext())
			{
				
				String line = reader.nextLine();
				if (!skippedFirstLine)
				{
					skippedFirstLine = true;
					data.add(line.split(","));
					continue;
				}
				
				
				if (line.contains("\""))
				{
					data.add(getRow(line));
				}
				else
				{
					data.add(line.split(","));
				}
				
			}
			reader.close();
			return data;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
	}
	
	static String getISO(String countryName)
	{
		String ISO = "NULL";
		if (countryISO.keySet().contains(countryName))
		{
			ISO = countryISO.get(countryName).toString();
		}
		else
		{
			for (String pattern : countryRegexISO.keySet())
			{
				Pattern p = Pattern.compile(pattern);
				Matcher matcher = p.matcher(countryName);
				if (matcher.find())
				{
					ISO = countryRegexISO.get(pattern).toString();
					break;
				}
			}
			
		}

		return ISO;
	}
	
	//In case a row has fields with " characters, we use this to extract them.
	static String[] getRow(String line)
	{
		
		String[] splitLine = new String[24];
		int currIndex = 0;
		String currString = "";
		boolean isInString = false;
		for (String i : line.split(""))
		{
			if (i.equals(","))
			{
				if (!isInString)
				{
					splitLine[currIndex] = currString;
					currIndex += 1;
					currString = "";
				}
				continue;
			}
			
			if (i.equals("\""))
			{
				isInString = !isInString;
			}

			currString += i;
		}
		
		return splitLine;
	}
	
	static boolean isNumeric(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
}
