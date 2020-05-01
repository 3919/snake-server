DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Devices;
CREATE TABLE Users (
    id INT NOT NULL AUTO_INCREMENT,
    login varchar(255) NOT NULL UNIQUE,
    pass_hash VARCHAR(512) NOT NULL,
    privilage INT NOT NULL,
    pin SMALLINT NOT NULL UNIQUE,
    user_name VARCHAR(255),
    user_surname VARCHAR(255),
    user_nick VARCHAR(255),
    valid_till DATE NOT NULL,
    rfid BLOB,
    PRIMARY KEY (id)
);

CREATE TABLE Devices (
    id INT NOT NULL AUTO_INCREMENT,
    device_name VARCHAR(80) NOT NULL UNIQUE,
    pass_hash VARCHAR(512) NOT NULL,
    privilage INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE MAC_ADDRESS (
    id INT NOT NULL AUTO_INCREMENT,
    login VARCHAR(80) NOT NULL UNIQUE,
    mac VARCHAR(512) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (login) REFERENCES users(login) 
);


INSERT INTO `pwr_snake`.`Users` (`login`, `pass_hash`, `privilage`, `pin`, `user_name`, `user_surname`, `user_nick`, `valid_till`, `rfid`) 
VALUES ('user1', '63b347973bb99fed9277b33cb4646b205e9a31331acfa574add3d2351f445e43', '0', '1234', 'Adrew', 'Golara', 'pogromca_kotletow94', '2021-01-01', 0x617364617364),
 ('user2', '63b347973bb99fed9277b33cb4646b205e9a31331acfa574add3d2351f445e43', '1', '4321', 'Zbigniew', 'Wodecki', 'pogromca_kotletow96', '2020-01-01', 0x617364617364);

INSERT INTO `pwr_snake`.`Devices`(`device_name`, `pass_hash`, `privilage`)
VALUES ('TMP36GT9Z', 'c053ed76ee0623662041c47d938a9497ea6d714d5818ea4c37fba36a4419731e','0'),
		('DHT11', 'c053ed76ee0623662041c47d938a9497ea6d714d5818ea4c37fba36a4419731e','0');