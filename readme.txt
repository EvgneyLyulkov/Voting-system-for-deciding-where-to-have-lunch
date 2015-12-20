
						Restaurant Menu Voter (RMV)
						
_______________________________________________________________________________________________
Initial task description:

Design and implement a JSON API using Hibernate/Spring/SpringMVC **without frontend**.

The task is:

Build a voting system for deciding where to have lunch.

 * 2 types of users: admin and regular users
 * Admin can input a restaurant and it's lunch menu of the day (2-5 items usually, just a dish name and price)
 * Menu changes each day (admins do the updates)
 * Users can vote on which restaurant they want to have lunch at
 * Only one vote counted per user
 * If user votes again the same day:
    - If it is before 11:00 we asume that he changed his mind.
    - If it is after 11:00 then it is too late, vote can't be changed

Each restaurant provides new menu each day.
_______________________________________________________________________________________________

Soultion and delivery suite.

A solution is implemented using Java and MS SQL Server.

Principal folders and files:
	Restaurant Menu Voter\Documentation\    -    design documentation including ER diagram for RVM database (in original SQLPowerArchitect format and also printed out as a pdf file).
	Restaurant Menu Voter\src\main\SQL\     -    SQL scripts to populate RMV database.
	Restaurant Menu Voter\src\main\java\org\largecode\rmvoting    -    Java classes:
		RMV_Database.java	-    RMV database interface;
		RMV_Exception.java  -    RMV specific exceptions handler;
	    JSON_Utils.java     -    aux JSON serialization routines (not specific to RMV).
		
Non-principal folders and classes:
	Restaurant Menu Voter                            -   command-line unit testing application;
	RestaurantMenuVoter_JSP_test                     -   web demo and testing application for RMV (also includes its own dedicated ReadMe.txt file). Tested with Apache Tomcat 7.0.
	Restaurant Menu Voter\src\main\java\org\json\    -   3-rd party JSON library from json.org\json\
	Restaurant Menu Voter\src\main\java\org\largecode\rmv_console_test\RestaurantMenuVoter_ConsoleTest.java    -    command line routine being used for for unit testing.
	
_______________________________________________________________________________________________

In case on any questions please contact Evgeny Lyulkov at
evgeny.lyulkov@gmail.com
Skype: Evgeny.Lyulkov 