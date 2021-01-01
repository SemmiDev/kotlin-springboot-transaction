delete from t_transaction;
delete from t_account;
delete from t_customer;

insert into t_customer(id, number, name) values ('n001', 'N-001', 'Customer 001');
insert into t_customer(id, number, name) values ('n002', 'N-002', 'Customer 002');

insert into t_account(id, id_customer, number, balance)values ('r001', 'n001', 'R-001', 0);
insert into t_account(id, id_customer, number, balance)values ('r002', 'n002', 'R-002',20000);
