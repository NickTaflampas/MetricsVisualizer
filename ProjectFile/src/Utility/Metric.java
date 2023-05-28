package Utility;

public class Metric {

	String displayName;
	String databaseName;
	String databaseTable;
	
	public Metric() {}
	
	public Metric(String displayName, String databaseName, String databaseTable)
	{
		this.displayName = displayName;
		this.databaseName = databaseName;
		this.databaseTable = databaseTable;
	}
	
	public Metric(String[] data)
	{
		this.displayName = data[0];
		this.databaseName = data[1];
		this.databaseTable = data[2];
	}

	
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseTable() {
		return databaseTable;
	}

	public void setDatabaseTable(String databaseTable) {
		this.databaseTable = databaseTable;
	}
}
