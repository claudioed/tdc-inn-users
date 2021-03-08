CREATE TABLE users
(
    id   VARCHAR(1000) NOT NULL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(1000) UNIQUE NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    blocked BOOLEAN NOT NULL DEFAULT false
);


insert into  users(id, first_name, last_name, email, email_verified, blocked) values ('41bf4517-5bc9-4a13-bdcd-11232371dbbf','joe','doe','joe@doe.com',false,false);
