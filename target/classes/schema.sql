drop table user if exists;
CREATE TABLE user(
id BIGINT AUTO_INCREMENT NOT NULL,
username VARCHAR(255) NOT NULL,
password VARCHAR(255) NOT NULL,
deposit BIGINT,
role VARCHAR(255) NOT NULL
);

ALTER TABLE user ADD CONSTRAINT USER_UNIQUE UNIQUE ( username );

drop table public.product if exists;
CREATE TABLE product(
id BIGINT AUTO_INCREMENT NOT NULL,
productname VARCHAR(255) NOT NULL,
amountavailable BIGINT,
cost BIGINT,
sellerid VARCHAR(255) NOT NULL
);

ALTER TABLE product ADD CONSTRAINT PRODUCT_UNIQUE UNIQUE ( productname,sellerid );



commit;

INSERT INTO user (id,username, password,deposit, role) VALUES (1,'rob', 'rob',50, 'BUYER');
--commit;
--INSERT INTO product (id,productname, amountavailable,cost, sellerid) VALUES (1,'car', 2, 500, 'FORD');
--commit;