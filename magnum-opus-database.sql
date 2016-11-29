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
	ID SERIAL PRIMARY KEY,
	userName varchar(64) NOT NULL,
	emailAddress varchar(64) NOT NULL
	);

CREATE TABLE Canvas (
	ID SERIAL PRIMARY KEY,
	painterID integer REFERENCES Painter(ID) NOT NULL,
	time timestamp NOT NULL,
	name varchar(50) NOT NULL
	);

CREATE TABLE Tile (
	ID SERIAL PRIMARY KEY,
	canvasID integer REFERENCES Canvas(ID) NOT NULL,
	xCoordinate integer NOT NULL,
	yCoordinate integer NOT NULL,
	data bytea NOT NULL,
	time timestamp NOT NULL,
	version integer NOT NULL,
	UNIQUE (canvasID, xCoordinate, yCoordinate)
	);

-- Allow users to select data from the tables.
GRANT SELECT ON Painter TO PUBLIC;
GRANT SELECT ON Canvas TO PUBLIC;
GRANT SELECT ON Tile TO PUBLIC;

-- Add sample records.
INSERT INTO Painter (userName, emailAddress) VALUES
	('hannahl95', '2016-11-01 08:00:00'),
	('alpha0010', '2016-11-01 08:00:00'),
	('caj7', '2016-11-01 08:00:00'),
	('JahnDavis27', '2016-11-01 08:00:00');

INSERT INTO Canvas (painterID, time, name) VALUES
	(1, '2016-11-01 08:00:00', 'Bob'),
	(2, '2016-11-01 08:00:00', 'Picture'),
	(4, '2016-11-01 08:00:00', 'Things Im Looking Forward To'),
	(3, '2016-11-01 08:00:00', 'Stuff');

INSERT INTO Tile (canvasID, xCoordinate, yCoordinate, data, time, version) VALUES
	(1, 25, 25, 'PNG data', '2016-11-01 08:00:00', 1),
	(1, 30, 35, 'PNG data', '2016-11-01 08:00:00', 2),
	(2, 15, 78, 'PNG data', '2016-11-01 08:00:00', 1),
	(2, 30, 58, 'PNG data', '2016-11-01 08:00:00', 2),
	(3, 34, 98, 'PNG data', '2016-11-01 08:00:00', 1),
	(4, 26, 83, 'PNG data', '2016-11-01 08:00:00', 1);

-- Add sample queries.

-- Select all the user names of the painters
SELECT userName FROM Painter;

-- Select the name of all the canvases created by the painter with user name 'alpha0010'
SELECT Canvas.name FROM Canvas JOIN Painter ON painterID = Painter.ID WHERE userName = 'alpha0010';

-- Select all the tiles for the canvas with id 1
SELECT * FROM Tile WHERE canvasID = 1;
