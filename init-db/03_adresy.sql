CREATE TABLE adresy (
	id_adresu SERIAL PRIMARY KEY,
	ulica VARCHAR(150),
	numer_domu VARCHAR(10),
	miasto VARCHAR(100) NOT NULL,
	kod_pocztowy VARCHAR(20),
	kraj VARCHAR(100) DEFAULT 'Polska'
);