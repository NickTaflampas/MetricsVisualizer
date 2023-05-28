<p align="center"> 
    <img height=170 src="https://cdn.discordapp.com/attachments/811721979539095563/1112397642627043429/yes.png"/> 
</p>
<h1> <div align="center">Country Metric Visualizer</div> </h1>
This is a completed project on the course of Advanced Topics in Database Technology and Applications. The goal of this project, is to manage a large amount of data, which is filtered, processed and visualized on the user's demand. This project makes use of the JavaFX Library for the visualization, as well as some minor libraries for reading the requested documents.
<hr style="width:50%;text-align:left;margin-left:0">  

<h1> <div align="center"> Goals and Usage </div> </h1>
The following are self-placed goals, as well as features requested for the purpose of this application and exercise:
<ul>
  <li> ETL Script :white_check_mark: </li>
  <li> DDL Script :white_check_mark: </li>
  <li> Database Backup :white_check_mark: </li>
  
  <li> BarChart of K Countries :white_check_mark: </li> 
  <li> BarChart of K Metrics :white_check_mark: </li>
  <li> LineChart of K Countries :white_check_mark: </li>
  <li> LineChart of K Metrics :white_check_mark: </li>
  <li> ScatterPlot of K Results :white_check_mark: </li>
  <li> Time Aggregation 5-year Interval :white_check_mark: </li>
  <li> Time Aggregation 10-year Interval :x: </li>
  
  <li> Report and Example of Analysis :white_check_mark: </li>
</ul>

<hr style="width:50%;text-align:left;margin-left:0">  
<h1> <div align="center"> Set-Up </div> </h1>
Several things must be done before running the application. First, one should load the needed libraries that are used throughout the code:

<h3> ETL Libraries </h3>
<a href="https://poi.apache.org/">Apache POI (XML Reading)</a>

<h3> DDL and Visualization Libraries </h3>
<a href="https://openjfx.io/">JavaFX</a><br>
MySQL Connector <br><br>

All libraries are included in a JAR form within the ETL-DDL-Libs folder, as well as the Javafx-sdk folder.

<h2> Download Data </h2>
The given ETL and DDL processes are used to process the instructed country data. Those data can be found on <a href="https://www.kaggle.com/">Kaggle</a> and should be placed on a folder whose path is known to you. <br>
<ul>
    <li> <a href="https://www.kaggle.com/datasets/sshashankrajak/countries"> Countries </a> </li>
    <li> <a href="https://www.kaggle.com/datasets/census/international-data"> Demographic Data </a> </li>
    <li> <a href="https://www.kaggle.com/datasets/frankmollard/income-by-country"> Economical Data </a> </li>
</ul>

<h2> Change Variable Paths </h2>
Lastly, change some final variables from within the code. One should have the following prepared:
<ul>
    <li> A path for the downloaded Data (IN_PATH) </li>
    <li> A path for the transformed Data (OUT_PATH or ETL_PATH) </li>
    <li> An empty database (name, path, password) </li>
</ul>
There is merit into making a config file for easier access, but for now:
<h3> Preprocessing.java </h3>
<p> 
    <img src="https://cdn.discordapp.com/attachments/473464812920373250/1112406421921874040/image.png"/> 
</p>

<h3> DDL.java </h3>
<p> 
    <img src="https://cdn.discordapp.com/attachments/473464812920373250/1112406565178318878/image.png"/> 
</p>

<h3> PlotController.java </h3>
<p> 
    <img src="https://cdn.discordapp.com/attachments/473464812920373250/1112406689845624872/image.png"/> 
</p>

<h1> <div align="center"> Video Demo </div> </h1>
A simple demo with a brief elaboration on the structure of this project, can be found <a href="https://www.youtube.com/watch?v=cohh_WfydZo">here.</a>

<h1> <div align="center"> Back-Up </div> </h1>
Several CSV files of the back-up data are included in the repository. They include the data after the ETL process.
