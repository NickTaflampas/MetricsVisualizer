package Preprocessing;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DDL {

	static final String ETL_PATH = "C://Users//nikos//Desktop//FixedData//";
	
	static Connection setConnection() throws Exception
	{
		String url = "jdbc:mysql://localhost:3306/countrydatabase";
		String name = "root";
		String password = "password";
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection(url, name, password);
		System.out.println("Connection formed successfully!");
		return con;
	}
	
	static void createTables(Connection connection) throws Exception
	{
		Statement statement = connection.createStatement();
		
		ResultSet r = statement.executeQuery("SHOW TABLES");
		//Drop all tables involved
		if (r.next())
		{
			String[] deletionQueries = new String[] {"DROP TABLE country_names_area;",
													"DROP TABLE birth_death_rates;",
													"DROP TABLE mortality_life_expectancy;",
													"DROP TABLE fertility_rates;",
													"DROP TABLE income_metrics;", 
													"DROP TABLE midyear_populations_by_range;", 
													"DROP TABLE midyear_populations;",
													"DROP TABLE midyear_populations_by_age;",
													"DROP TABLE countries;"};
			
			for (String query : deletionQueries)
			{
				statement.addBatch(query);
			}
		}
		
		
		//Create all needed tables
		String[] creationQueries = new String[] 
				{"CREATE TABLE countries( iso VARCHAR(2), iso3 VARCHAR(3), iso_code INT, fips VARCHAR(2), display_name VARCHAR(255), official_name VARCHAR(255), capital_name VARCHAR(255), continent_name VARCHAR(255), currency_code VARCHAR(3), currency_name VARCHAR(255), phone INT, region_code INT, region_name VARCHAR(255), subregion_code INT, subregion_name VARCHAR(255), intermediate_region_code INT, intermediate_region_name VARCHAR(255), current_status VARCHAR(255), development_status VARCHAR(20), sids BOOL, lldc BOOL, ldc bool, area_square_km FLOAT, population INT, primary key(iso_code) );",
				"CREATE TABLE country_names_area( iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), country_area FLOAT, foreign key(iso_code) references countries(iso_code), primary key(iso_code) );",
				"CREATE TABLE birth_death_rates( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), metric_year INT, crude_birth_rate FLOAT, crude_death_rate FLOAT, net_migration FLOAT, rate_natural_increase FLOAT, growth_rate FLOAT, foreign key(iso_code) references countries(iso_code), primary key(id) );",
				"CREATE TABLE mortality_life_expectancy( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), metric_year INT, infant_mortality FLOAT, infant_mortality_M FLOAT, infant_mortality_F FLOAT, life_expectancy FLOAT, life_expectancy_M FLOAT, life_expectancy_F FLOAT, mortality_under5 FLOAT, mortality_under5_M FLOAT, mortality_under5_F FLOAT, mortality_1to4 FLOAT, mortality_1to4_M FLOAT, mortality_1to4_F FLOAT, foreign key(iso_code) references countries(iso_code), primary key(id) );",
				"CREATE TABLE fertility_rates( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), metric_year INT, fertility_15to19 FLOAT, fertility_20to24 FLOAT, fertility_25to29 FLOAT, fertility_30to34 FLOAT, fertility_35to39 FLOAT, fertility_40to44 FLOAT, fertility_45_49 FLOAT, total_fertility_rate FLOAT, gross_reproduction_rate FLOAT, sex_birth_ratio FLOAT, foreign key(iso_code) references countries(iso_code), primary key(id) );",
				"CREATE TABLE income_metrics( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_name VARCHAR(255), metric_year INT, income_index FLOAT, labour_share_GDP FLOAT, gross_fixed_capital_formation FLOAT, GDP_total FLOAT, GDP_per_capita FLOAT, GNI_per_capita FLOAT, GNI_estimate_male FLOAT, GNI_estimate_female FLOAT, domestic_credit FLOAT, foreign key (iso_code) references countries(iso_code), primary key(id) );",
				"CREATE TABLE midyear_populations( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), metric_year INT, population INT, foreign key (iso_code) references countries(iso_code), primary key(id) );",
				"CREATE TABLE midyear_populations_by_range( id INT NOT NULL AUTO_INCREMENT, iso_code INT, country_code VARCHAR(2), country_name VARCHAR(255), metric_year INT, starting_age INT, ending_age INT, population INT, male_population INT, female_population INT, foreign key (iso_code) references countries(iso_code), primary key(id) );"};
	
		for (String query : creationQueries)
		{
			statement.addBatch(query);
		}
		statement.executeBatch();
		
		String query = "CREATE TABLE midyear_populations_by_age(id INT NOT NULL AUTO_INCREMENT,iso_code INT,country_name VARCHAR(255),metric_year INT,sex VARCHAR(6),"; //,foreign key (iso_code) references countries(iso_code),primary key(id));" + 
		for (int i = 0; i < 101; i++)
		{
			query += "age_" + i + "_population INT,";
		}
		query += "foreign key (iso_code) references countries(iso_code),primary key(id));";
		statement.execute(query);
		System.out.println("Tables created.");
		
	}
	
	static void loadData(Connection connection) throws Exception
	{
		Statement statement = connection.createStatement();
		
		String[] loadQueries = new String[]
				{
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_countries.csv' INTO TABLE countries FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"' LINES TERMINATED BY '\\n' (iso,iso3,iso_code,fips,display_name,@official_name,capital_name,continent_name, currency_code,currency_name,phone,region_code,region_name,subregion_code,subregion_name, intermediate_region_code,intermediate_region_name,current_status,development_status,sids,lldc,ldc,area_square_km,population) SET official_name = convert(cast(@official_name as binary) using latin1);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_country_names_area.csv' INTO TABLE country_names_area FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES;",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_birth_death_growth_rates.csv' INTO TABLE birth_death_rates FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_code,country_name,metric_year,crude_birth_rate, crude_death_rate,net_migration,rate_natural_increase,growth_rate);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_mortality_life_expectancy.csv' INTO TABLE mortality_life_expectancy FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_code,country_name,metric_year,infant_mortality,infant_mortality_M,infant_mortality_F, life_expectancy,life_expectancy_M,life_expectancy_F,mortality_under5,mortality_under5_M, mortality_under5_F,mortality_1to4,mortality_1to4_M,mortality_1to4_F);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_age_specific_fertility_rates.csv' INTO TABLE fertility_rates FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_code,country_name,metric_year,fertility_15to19,fertility_20to24,fertility_25to29, fertility_30to34,fertility_35to39,fertility_40to44,fertility_45_49,total_fertility_rate, gross_reproduction_rate,sex_birth_ratio);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_income.csv' INTO TABLE income_metrics FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_name,metric_year,income_index,labour_share_GDP,gross_fixed_capital_formation, GDP_total,GDP_per_capita,GNI_per_capita,GNI_estimate_male,GNI_estimate_female,domestic_credit);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_midyear_population.csv' INTO TABLE midyear_populations FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_code,country_name,metric_year,population);",
						"LOAD DATA INFILE '" + ETL_PATH + "ETL_midyear_population_5yr_age_sex.csv' INTO TABLE midyear_populations_by_range FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_code,country_name,metric_year,starting_age,ending_age, population,male_population,female_population);"
				};
		
		for (String query : loadQueries)
		{
			statement.addBatch(query);
		}
		statement.executeBatch();
		
		
		String query = "LOAD DATA INFILE '" + ETL_PATH + "ETL_midyear_big_population.csv'" +
				" INTO TABLE midyear_populations_by_age FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"'" +
				"LINES TERMINATED BY '\\n' IGNORE 1 LINES (iso_code,country_name,metric_year,sex,";
		for (int i = 0; i < 101; i++)
		{
			query += "age_" + i + "_population,";
		}
		query = query.substring(0, query.length()-1) + ");";
				
		statement.execute(query);
		
		System.out.println("Data loaded.");
	}
	
	public static void main(String[] args)
	{
		
		try
		{
			Connection con = setConnection();
			createTables(con);
			loadData(con);
		}
		catch (Exception e)
		{
			System.out.println(":( " + e);
		}
		
	}
}
