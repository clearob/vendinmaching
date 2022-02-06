

INSERT INTO user(id,username, password,deposit, role) VALUES (1,'rob', 'rob',50, 'ADMIN');
commit;
INSERT INTO product(id,productname, amountavailable,cost, sellerid) VALUES (1,'car', 2, 500, 'FORD');
commit;