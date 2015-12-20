<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Restaurant Menu Voter</title>
</head>
<body>
	<h1>Restaurant Menu Voter</h1>
	<h2>Testing stuff</h2>
	<hr />

	<form style="border-style: outset;" action="RV_test">
		Restaurant: <input type="text" name="restaurant"/><Br> 
		Dish: <input type="text" name="dish"/>
		Price: <input type="text" name="price" /><Br> 
		Dish: <input type="text" name="dish"/>
		Price: <input type="text" name="price" /><Br> 
		Dish: <input type="text" name="dish"/>
		Price: <input type="text" name="price" /><Br> 
		<Br><input type="submit" name="addMenu" value="Add Menu" /><Br>
	</form>
	<hr />
	
	<form style="border-style: outset;" action="RV_test">
		Name: <input type="text" name="name"/>
		Surname: <input type="text" name="surname"/>
		Restaurant: <input type="text" name="restaurant"/> 
		<br>Grade:<br> 
		<input type="radio" name="grade" value="5"/>Excellent<Br> 
		<input type="radio" name="grade" value="4"/>Good<Br> 
		<input type="radio" name="grade" value="3" checked="checked"/>Ambiguous<Br> 
		<input type="radio" name="grade" value="2"/>Bad<Br> 
		<input type="radio" name="grade" value="1"/>Disgusting<Br> 
		<Br><input type="submit" name="vote" value="Vote"/><Br>
	</form>
	
	<hr />
	
	<form style="border-style: outset;" action="RV_test">
		<Br><input type="submit" name="Reports" value="Reports..."/><Br>
	</form>
	
<%--
	<button type="button" onclick="getVisitors()">Visitors</button>
	<button type="button" onclick="getReports()">Reports</button>

	<p id="demo">placeholder</p>
	<p id="list" />

	<script type="text/javascript" src="RestaurantMenuVoter.js"></script> 
--%>

</body>
</html>