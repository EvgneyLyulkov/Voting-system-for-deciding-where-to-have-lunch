==================================================================================================
This bundle is a JSP-based testing suite for Restaurant Menu Voter (RMV) database and application.
==================================================================================================

Prerequisites:

1. Copy the entire bundle content to Web server data folder e.g. %CATALINA_HOME%\wtpwebapps for Apache Tomcat.

2. Install a local MS SQL Server v.2005 or higher or establish a connection to a remote MS SQL server.

3. Make sure that you have a MS SQL Server database populated by RMV tables. 
If this DB does not exist yet, create a database or select an existing one and populate it using "Restaurant Menu Voter.sql" file. 
(This SQL script is not included in this deployment bundle. It is coming along with Java sources files in a separate bundle).
  
4. Open "RestaurantMenuVoter_JSP_test\WEB-INF\RestaurantMenuVoter_JSP_test.properties" file and set proper SQL_DB_CONNECTION_STRING value reflecting your RMV database.

5. Start your WEB server if not running.

6. Open Internet browser and type a proper URL like	http://localhost:8080/RestaurantMenuVoter_JSP_test/  

==================================================================================================

In case on any questions please contact Evgeny Lyulkov at
evgeny.lyulkov@gmail.com
Skype: Evgeny.Lyulkov 
 


 