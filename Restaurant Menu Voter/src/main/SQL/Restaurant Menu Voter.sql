/*
Restaurant Menu Voter (RMV).
 
Voting system for deciding where to have lunch.
LargeCode trial task.

RMV database population script.
Applicable to MS SQL Server 2005 through current version

Author: Evgeny Lyulkov
Revision 1.0	Dec.15 2015
*/

CREATE TABLE Grade (
                Value INT NOT NULL CONSTRAINT Grade_PK PRIMARY KEY,
                Name VARCHAR(32) NOT NULL CONSTRAINT Grade_AK UNIQUE
)


CREATE TABLE Dish (
                Id INT IDENTITY NOT NULL CONSTRAINT Dish_PK PRIMARY KEY,
                Name VARCHAR(128) NOT NULL CONSTRAINT Dish_AK UNIQUE
)


CREATE TABLE Restaurant (
                Id INT IDENTITY NOT NULL CONSTRAINT Restaurant_PK PRIMARY KEY,
                Name VARCHAR(128) NOT NULL CONSTRAINT Restaurant_�� UNIQUE CONSTRAINT Restaurant_Name_Empty CHECK (Name <> '')
)


CREATE TABLE Menu (
                Id INT IDENTITY NOT NULL CONSTRAINT Menu_PK PRIMARY KEY,
                Restaurant INT NOT NULL,
                Since DATETIME DEFAULT GETDATE() NOT NULL,
				CONSTRAINT Menu_AK UNIQUE ( Restaurant, Since ) -- Menu changes only once a day
)


CREATE TABLE MenuItem (
                Menu INT NOT NULL,
                Dish INT NOT NULL,
                Price FLOAT NOT NULL,
                CONSTRAINT MenuItem_PK PRIMARY KEY (Menu, Dish)
)

CREATE TABLE Visitor (
                Id INT IDENTITY NOT NULL CONSTRAINT Visitor_PK PRIMARY KEY,
                Name VARCHAR(32) NOT NULL,
                Surname VARCHAR(32) NOT NULL,
				FullName AS Name + ' ' + Surname, 
				CONSTRAINT Visitor_AK UNIQUE ( Name, Surname ),
				CONSTRAINT Visitor_Name_Empty CHECK (Name + Surname <> '')
)


CREATE TABLE Vote (
                Visitor INT NOT NULL,
                Menu INT NOT NULL,
                Grade INT NOT NULL,
                VoteDate DATETIME DEFAULT GETDATE() NOT NULL,
                CONSTRAINT Vote_PK PRIMARY KEY (Visitor, Menu)
)

-- The Report table contains a list of predefined reports along with report names and corresponding SELECT queries.
CREATE TABLE Report (
                Id INT IDENTITY NOT NULL CONSTRAINT Report_PK PRIMARY KEY,
                Name VARCHAR(128) NOT NULL CONSTRAINT Report_AK UNIQUE,
                Query VARCHAR(2048) NOT NULL -- SQL SELECT Query
)

GO

-- Foreign keys --

ALTER TABLE Vote ADD CONSTRAINT Grade_Vote_FK
	FOREIGN KEY (Grade) REFERENCES Grade (Value)
	ON DELETE NO ACTION

ALTER TABLE MenuItem ADD CONSTRAINT Dish_MenuItem_FK
	FOREIGN KEY (Dish) REFERENCES Dish (Id)
	ON DELETE CASCADE

ALTER TABLE Menu ADD CONSTRAINT Restaurant_Menu_FK
	FOREIGN KEY (Restaurant) REFERENCES Restaurant (Id)
	ON DELETE CASCADE

ALTER TABLE MenuItem ADD CONSTRAINT Menu_MenuItem_FK
	FOREIGN KEY (Menu) REFERENCES Menu (Id)
	ON DELETE CASCADE

ALTER TABLE Vote ADD CONSTRAINT Menu_Vote_FK
	FOREIGN KEY (Menu) REFERENCES Menu (Id)

ALTER TABLE Vote ADD CONSTRAINT Visitor_Vote_fk
	FOREIGN KEY (Visitor) REFERENCES Visitor (Id)

GO

-- Aux data types --

CREATE TYPE Ids AS TABLE
(
    Id INT
);
GO

-- Procedures --

CREATE PROCEDURE spCheckMenuAlterability (@updatedMenus AS Ids READONLY) AS
	-- check #1: Only the most recent menu can be changed:
	IF EXISTS (
		SELECT TOP 1 1 
			FROM @updatedMenus um
			INNER JOIN Menu m ON m.Id = um.Id
			INNER JOIN Menu otherMenus ON otherMenus.Restaurant = m.Restaurant AND m.Since < otherMenus.Since 
		)
		RAISERROR ('Menu update declined. Only the most recent menu can be changed.', 11, 101)	

	-- check #2: Menu can be changed only if it is not voted yet:
	IF EXISTS (
		SELECT TOP 1 1 
			FROM @updatedMenus um
			INNER JOIN Vote v ON v.Menu = um.Id 
		)
		RAISERROR ('Menu update declined. Menu is alredy voted by users, therefore it can not be changed anymore.', 11, 103)	
GO

-- Triggers --

CREATE TRIGGER MenuUpdate ON Menu FOR UPDATE NOT FOR REPLICATION AS  
BEGIN
	DECLARE @ids Ids;
	INSERT INTO @ids(Id)
		SELECT Id FROM deleted;
    EXEC spCheckMenuAlterability @ids;
END;
GO

CREATE TRIGGER MenuItemInsert ON MenuItem FOR INSERT NOT FOR REPLICATION AS  
BEGIN
	DECLARE @ids Ids;
	INSERT INTO @ids(Id)
		SELECT Menu FROM inserted;
    EXEC spCheckMenuAlterability @ids;
END;
GO

CREATE TRIGGER MenuItemUpdate ON MenuItem FOR UPDATE NOT FOR REPLICATION AS  
BEGIN
	DECLARE @ids Ids;
	INSERT INTO @ids(Id)
		SELECT Menu FROM deleted;
    EXEC spCheckMenuAlterability @ids;
END;
GO

CREATE TRIGGER MenuItemDelete ON MenuItem FOR DELETE NOT FOR REPLICATION AS  
BEGIN
	DECLARE @ids Ids;
	INSERT INTO @ids(Id)
		SELECT Menu FROM deleted;
    EXEC spCheckMenuAlterability @ids;
END;
GO

/*
From original feature specification:
If user votes again the same day:
    - If it is before 11:00 we asume that he changed his mind.
    - If it is after 11:00 then it is too late, vote can't be changed
*/
CREATE TRIGGER VoteUpdate ON Vote FOR UPDATE NOT FOR REPLICATION AS  
	DECLARE @hours INT
	SET @hours = DATEPART(hour, GETDATE());
	IF (@hours > 11)
		RAISERROR ('Vote update is declined because it is too late.', 11, 127)	
GO

-- UI vocabulary population --

INSERT INTO Grade (Value, Name)
	VALUES 
		(1, 'Disgusting'),
		(2, 'Bad'),
		(3, 'Ambiguous'),
		(4, 'Good'),
		(5, 'Excellent');
GO

-- Predefined reports --
INSERT INTO Report(Name, Query)	VALUES
('Voting grade scale',
'SELECT Value, Name FROM Grade ORDER BY Value desc'); 

INSERT INTO Report(Name, Query)	VALUES
('Restaurants (retrospective voting review)',
'WITH RestCTE AS ( ' +
'SELECT r.Id [Restaurant], m.Id [Menu] ' +
'	FROM Restaurant r ' +
'	LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id  ' +
'	GROUP BY r.Id, m.Id),  ' +
'VoteCTE AS ( ' + 
'SELECT r.[Restaurant],   ' +
'	COUNT(v.Grade) Votes, MIN(v.Grade) [Worst Vote], MAX(v.Grade) [Best Vote], AVG(CAST (v.Grade AS decimal) ) [Avg Vote]   ' +
'	FROM RestCTE r ' +
'	LEFT OUTER JOIN Vote v ON v.Menu = r.[Menu] ' +
'	GROUP BY r.[Restaurant]), ' +
'DishCTE AS ( ' + 
'SELECT r.[Restaurant],  ' +
'	Count(DISTINCT r.[Menu]) [Menus], Count(DISTINCT d.Id) [Dishes] ' +
'	FROM RestCTE r ' +
'	LEFT OUTER JOIN MenuItem mi ON mi.Menu = r.[Menu] ' +
'	LEFT OUTER JOIN Dish d ON d.Id = mi.Dish ' +
'	GROUP BY r.[Restaurant]) ' +
'SELECT r.Name [Restaurant], [Menus], [Dishes], Votes, gmin.Name + '' ('' + LTRIM(STR([Worst Vote])) + '')'' [Worst Vote], gmax.Name + '' ('' + LTRIM(STR([Best Vote])) + '')'' [Best Vote], [Avg Vote] ' +
'	FROM Restaurant r ' +
'	INNER Join DishCTE d ON d.[Restaurant] = r.Id ' +
'	INNER JOIN VoteCTE v ON v.[Restaurant] = r.Id ' +
'	LEFT OUTER JOIN Grade gmin ON gmin.Value = v.[Worst Vote] ' +
'	LEFT OUTER JOIN Grade gmax ON gmax.Value = v.[Best Vote] ' +
'	ORDER BY 1');

INSERT INTO Report(Name, Query)	VALUES
('Restaurants (voting review for present menu)',
'WITH RestCTE AS ( ' +
'SELECT r.Id [Restaurant], m.Id [Menu] ' +
'	FROM Restaurant r ' +
'	LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id AND m.Since = (SELECT MAX(Since) FROM Menu mm WHERE mm.Restaurant = m.Restaurant) ' + 
'	GROUP BY r.Id, m.Id),  ' +
'VoteCTE AS ( ' + 
'SELECT r.[Restaurant],   ' +
'	COUNT(v.Grade) Votes, MIN(v.Grade) [Worst Vote], MAX(v.Grade) [Best Vote], AVG(CAST (v.Grade AS decimal) ) [Avg Vote]  ' +
'	FROM RestCTE r ' +
'	LEFT OUTER JOIN Vote v ON v.Menu = r.[Menu] ' +
'	GROUP BY r.[Restaurant]), ' +
'DishCTE AS ( ' +  
'SELECT r.[Restaurant], Count(DISTINCT d.Id) Dishes ' +
'	FROM RestCTE r ' +
'	LEFT OUTER JOIN MenuItem mi ON mi.Menu = r.[Menu] ' +
'	LEFT OUTER JOIN Dish d ON d.Id = mi.Dish ' +
'	GROUP BY r.[Restaurant]) ' +
'SELECT r.Name [Restaurant], Dishes, Votes, gmin.Name + '' ('' + LTRIM(STR([Worst Vote])) + '')'' [Worst Vote], gmax.Name + '' ('' + LTRIM(STR([Best Vote])) + '')'' [Best Vote], [Avg Vote] ' +
'	FROM Restaurant r ' +
'	INNER Join DishCTE d ON d.[Restaurant] = r.Id ' +
'	INNER JOIN VoteCTE v ON v.[Restaurant] = r.Id ' +
'	LEFT OUTER JOIN Grade gmin ON gmin.Value = v.[Worst Vote] ' +
'	LEFT OUTER JOIN Grade gmax ON gmax.Value = v.[Best Vote] ' +
'	ORDER BY 1');

INSERT INTO Report(Name, Query)	VALUES
('Visitors',
'WITH MyCTE AS ( ' +
'SELECT vr.FullName [Visitor], COUNT(DISTINCT r.Id) [Restaurants visited],  ' +
'	COUNT(v.Grade) [Total Votes], Min(v.Grade) [Worst Vote], MAX(v.Grade) [Best Vote], AVG(CAST (v.Grade AS decimal) ) [Avg Vote]  ' +
'	FROM Visitor vr  ' +
'	LEFT OUTER JOIN Vote v ON v.Visitor = vr.Id ' +
'	LEFT OUTER JOIN Menu m ON m.Id = v.Menu ' +
'	LEFT OUTER JOIN Restaurant r ON r.Id = m.Restaurant ' +
'	GROUP BY vr.FullName) ' +
'SELECT [Visitor], [Restaurants visited], [Total Votes], gmin.Name + '' ('' + LTRIM(STR([Worst Vote])) + '')'' [Worst Vote], gmax.Name + '' ('' + LTRIM(STR([Best Vote])) + '')'' [Best Vote], [Avg Vote]  ' +
'	FROM MyCTE m ' +
'	LEFT OUTER JOIN Grade gmin ON gmin.Value = m.[Worst Vote] ' +
'	LEFT OUTER JOIN Grade gmax ON gmax.Value = m.[Best Vote] ' +
'	ORDER BY 1 ');

INSERT INTO Report(Name, Query)	VALUES
('Present day menu',
'SELECT r.Name [Restaurant], d.Name [Dish], mi.Price ' +
'	FROM Restaurant r ' +
'	LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id AND m.Since = (SELECT MAX(Since) FROM Menu mm WHERE mm.Restaurant = m.Restaurant) ' +
'	LEFT OUTER JOIN MenuItem mi ON mi.Menu = m.Id ' +
'	LEFT OUTER JOIN Dish d ON d.Id = mi.Dish ' +
'	ORDER BY 1, 2, 3 ');

INSERT INTO Report(Name, Query)	VALUES
('Detailed voting information',
'SELECT r.Name [Restaurant], m.Since [Menu date], vr.FullName [Visitor], v.VoteDate, g.Name [Vote] ' +
'	FROM Restaurant r ' +
'	LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id ' +
'	LEFT OUTER JOIN Vote v ON v.Menu = m.Id ' +
'	LEFT OUTER JOIN Visitor vr ON vr.Id = v.Visitor ' +
'	LEFT OUTER JOIN Grade g ON g.Value = v.Grade ' +
'	ORDER BY 1, 2, 3, 4');

INSERT INTO Report(Name, Query)	VALUES
('Present day menu votes',
'SELECT r.Name [Restaurant], m.Since [Menu date], vr.FullName [Visitor], v.VoteDate [Vote Date], g.Name [Vote] ' +
'	FROM Restaurant r ' +
'	LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id AND m.Since = (SELECT MAX(Since) FROM Menu mm WHERE mm.Restaurant = m.Restaurant) ' +
'	LEFT OUTER JOIN Vote v ON v.Menu = m.Id ' +
'	LEFT OUTER JOIN Visitor vr ON vr.Id = v.Visitor ' +
'	LEFT OUTER JOIN Grade g ON g.Value = v.Grade ' +
'	ORDER BY 1, 2, 3, 4');

INSERT INTO Report(Name, Query)	VALUES
('Restaurants ordered by sum od visitor''s votes',
'SELECT r.Name [Restaurant], COUNT(v.Grade) [Vote Count], SUM(v.Grade) [Sum(grade)], CAST (SUM(v.Grade) AS decimal) / COUNT(v.Grade) [Sum(grade) / Vote Count] ' +
'	 FROM Restaurant r LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id LEFT OUTER JOIN Vote v ON v.Menu = m.Id GROUP BY r.Name ORDER BY 4 desc, r.Name');

INSERT INTO Report(Name, Query)	VALUES
('Restaurants ordered by visitor''s votes for present day menu',
'SELECT r.Name [Restaurant], COUNT(v.Grade) [Vote Count], SUM(v.Grade) [Sum(grade)],  CAST( SUM(v.Grade) AS decimal) / COUNT(v.Grade) [Sum(grade) / Vote Count] ' +
'	FROM Restaurant r LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id AND m.Since = (SELECT MAX(Since) FROM Menu mm WHERE mm.Restaurant = m.Restaurant) ' +
	'	 LEFT OUTER JOIN Vote v ON v.Menu = m.Id GROUP BY r.Name ORDER BY 4 desc, r.Name; ');

INSERT INTO Report(Name, Query)	VALUES
('Restaurants ordered by sum of visitor''s votes weighted by average vote of the same visitor',
'WITH VisitorCTE AS ( ' +
'SELECT vr.Id, vr.FullName, AVG(v.Grade) AVG_Grade ' +
'	FROM Visitor vr ' +
'	LEFT OUTER JOIN Vote v on v.Visitor = vr.Id ' +
'	GROUP BY vr.Id, vr.FullName) ' +
'SELECT r.Name [Restaurant], COUNT(v.Grade) [Vote Count], SUM(CAST (v.Grade AS decimal) / vr.AVG_Grade) [Sum(grade) / Avg(visitor''s grade)] ' +
	'	 FROM Restaurant r  ' +
	'	 LEFT OUTER JOIN Menu m ON m.Restaurant = r.Id  ' +
	'	 LEFT OUTER JOIN Vote v ON v.Menu = m.Id  ' +
	'	 LEFT OUTER JOIN VisitorCTE vr ON vr.Id = v.Visitor  ' +
	'	 GROUP BY r.Name ORDER BY 3 desc, r.Name');
