DROP TABLE IF EXISTS MAC_MAP;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Devices;

CREATE TABLE Users (
    id INT NOT NULL AUTO_INCREMENT,
    login varchar(255) NOT NULL UNIQUE,
    pass_hash VARCHAR(512) NOT NULL,
    privilege INT NOT NULL,
    pin SMALLINT NOT NULL UNIQUE,
    user_name VARCHAR(255),
    user_surname VARCHAR(255),
    user_nick VARCHAR(255),
    valid_till DATE NOT NULL,
    user_email VARCHAR(255)NOT NULL UNIQUE,
    rfid BLOB,
    PRIMARY KEY (id)
);
CREATE TABLE Devices(
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(80) NOT NULL UNIQUE,
	type int NOT NULL,
	token VARCHAR(20) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE MAC_MAP (
    id INT NOT NULL AUTO_INCREMENT,
    login VARCHAR(80) NOT NULL UNIQUE,
    mac VARCHAR(13) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);


INSERT INTO `pwr_snake`.`Users` (`login`, `pass_hash`, `privilege`, `pin`, `user_email`, `user_name`, `user_surname`, `user_nick`, `valid_till`, `rfid`) 
VALUES ('user1', '63b347973bb99fed9277b33cb4646b205e9a31331acfa574add3d2351f445e43', '1', '1234', 'adrew.golara@gmail.com', 'Adrew', 'Golara', 'pogromca_kotletow94', '2021-01-01', 0x617364617364),
('user3', '63b347973bb99fed9277b33cb4646b205e9a31331acfa574add3d2351f445e43', '0', '1274', 'adrew.golara2@gmail.com', 'Adrew', 'Golara', 'pogromca_kotletow94', '2021-01-01', 0x417364617364),
 ('user2', '63b347973bb99fed9277b33cb4646b205e9a31331acfa574add3d2351f445e43', '1', '4321', 'abbzibzi@gmail.com', 'Zbigniew', 'Wodecki', 'pogromca_kotletow96', '2020-01-01', 0x617164613364);


INSERT  INTO `pwr_snake`.`Devices` (`name`, `type`, `token`)
VALUES ('LM35DZ', 0, 'WuU43xmFZsYpSZXbkI6l'),
	   ('DHT11', 1, 'EPWw05ahiHfBoPiQupRL'),
	  ('WINDOW_LAB', 2, 'fjTAqEWwW4VH1J05KxTj'),
	 ('WINDOW_SOC', 2, 'X8jpzDpmLvCedjy2Wa6l');

INSERT  INTO `pwr_snake`.`MAC_MAP` (`login`, `mac`)
VALUES ('user1', '000AE63EFDE1'),
	   ('user2', '100AE63EFDD1');