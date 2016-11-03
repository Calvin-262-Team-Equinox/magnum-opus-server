--
-- This SQL script builds a database for the Magnum Opus Drawing Application.
--
-- @author hludema
--

-- Drop previous versions of the tables if they they exist, in reverse order of foreign keys.
DROP TABLE IF EXISTS Tile;
DROP TABLE IF EXISTS Canvas;
DROP TABLE IF EXISTS Painter;

-- Create the schema.
CREATE TABLE Painter (
	ID integer PRIMARY KEY, 
	userName varchar(64) NOT NULL,
	emailAddress varchar(64) NOT NULL
	);

CREATE TABLE Canvas (
	ID integer PRIMARY KEY, 
	painterID integer REFERENCES Painter(ID) NOT NULL,
	time timestamp NOT NULL,
	name varchar(50) NOT NULL
	);

CREATE TABLE Tile (
	ID integer PRIMARY KEY, 
	canvasID integer REFERENCES Canvas(ID) NOT NULL,
	xCoordinate integer NOT NULL,
	yCoordinate integer NOT NULL,
	data bytea NOT NULL,
	time timestamp NOT NULL,
	version integer NOT NULL
	);

-- Allow users to select data from the tables.
GRANT SELECT ON Painter TO PUBLIC;
GRANT SELECT ON Canvas TO PUBLIC;
GRANT SELECT ON Tile TO PUBLIC;

-- Add sample records.
INSERT INTO Painter VALUES (1, 'hannahl95', '2016-11-01 08:00:00');
INSERT INTO Painter VALUES (2, 'alpha0010', '2016-11-01 08:00:00');
INSERT INTO Painter VALUES (3, 'caj7', '2016-11-01 08:00:00');
INSERT INTO Painter VALUES (4, 'JahnDavis27', '2016-11-01 08:00:00');

INSERT INTO Canvas VALUES (1, 1, '2016-11-01 08:00:00', 'Bob');
INSERT INTO Canvas VALUES (2, 2, '2016-11-01 08:00:00', 'Picture');
INSERT INTO Canvas VALUES (3, 4, '2016-11-01 08:00:00', 'Things Im Looking Forward To');
INSERT INTO Canvas VALUES (4, 3, '2016-11-01 08:00:00', 'Stuff');

INSERT INTO Tile VALUES (1, 1, 25, 25, 'PNG data', '2016-11-01 08:00:00', 1);
INSERT INTO Tile VALUES (2, 1, 30, 35, 'PNG data', '2016-11-01 08:00:00', 2);
INSERT INTO Tile VALUES (3, 2, 15, 78, 'PNG data', '2016-11-01 08:00:00', 1);
INSERT INTO Tile VALUES (4, 2, 30, 58, 'PNG data', '2016-11-01 08:00:00', 2);
INSERT INTO Tile VALUES (5, 3, 34, 98, 'PNG data', '2016-11-01 08:00:00', 1);
INSERT INTO Tile VALUES (6, 4, 26, 83, 'PNG data', '2016-11-01 08:00:00', 1);

-- Add sample queries.

-- Select all the user names of the painters
SELECT userName FROM Painter;

-- Select the name of all the canvases created by the painter with user name 'alpha0010'
SELECT Canvas.name FROM Canvas JOIN Painter ON painterID = Painter.ID WHERE userName = 'alpha0010';

-- Select all the tiles for the canvas with id 1
SELECT * FROM Tile WHERE canvasID = 1; 
