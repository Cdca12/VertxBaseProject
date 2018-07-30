alter table CC_persona modify created_at TIMESTAMP not null default CURRENT_TIMESTAMP;
alter table CC_comida modify created_at TIMESTAMP not null default CURRENT_TIMESTAMP;
alter table CC_animal modify created_at TIMESTAMP not null default CURRENT_TIMESTAMP;
alter table CC_animales_comidas modify created_at TIMESTAMP not null default CURRENT_TIMESTAMP;

alter table CC_persona add column status int not null default 1;
alter table CC_persona add column created_at datetime not null;
alter table CC_persona add column created_by int not null;
alter table CC_persona add column updated_at datetime;
alter table CC_persona add column updated_by int;

alter table CC_comida add column status int not null default 1;
alter table CC_comida add column created_at datetime not null;
alter table CC_comida add column created_by int not null;
alter table CC_comida add column updated_at datetime;
alter table CC_comida add column updated_by int;

alter table CC_animal add column status int not null default 1;
alter table CC_animal add column created_at datetime not null;
alter table CC_animal add column created_by int not null;
alter table CC_animal add column updated_at datetime;
alter table CC_animal add column updated_by int;

alter table CC_animales_comidas add column status int not null default 1;
alter table CC_animales_comidas add column created_at datetime not null;
alter table CC_animales_comidas add column created_by int not null;
alter table CC_animales_comidas add column updated_at datetime;
alter table CC_animales_comidas add column updated_by int;

SELECT * FROM CC_persona
SELECT * FROM CC_animal
SELECT * FROM CC_comida
SELECT * FROM CC_animales_comidas