

CREATE TABLE CC_persona(
	ID int NOT NULL PRIMARY KEY auto_increment,
	nombre VARCHAR(50) NOT NULL
);

CREATE TABLE CC_animal(
	ID int NOT NULL PRIMARY KEY auto_increment,
	nombre VARCHAR(50) NOT NULL,
	
	CC_persona_ID int NOT NULL,
	CONSTRAINT fk_CC_animal_persona FOREIGN KEY (CC_persona_ID) REFERENCES CC_persona(ID)
);

CREATE TABLE CC_comida(
	ID int NOT NULL PRIMARY KEY auto_increment,
	nombre VARCHAR(50) NOT NULL
);

CREATE TABLE CC_animales_comidas(
	CC_animal_ID int NOT NULL,
	CC_comida_ID int NOT NULL,
	CONSTRAINT fk__CC_animal_comida FOREIGN KEY (CC_animal_ID) REFERENCES CC_animal(ID),
	constraint fk_CC_comida_animal FOREIGN KEY (CC_comida_ID) REFERENCES CC_comida(ID)
)
