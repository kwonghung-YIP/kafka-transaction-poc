insert into account (
    id, name, balance
) values (
    gen_random_uuid(),'john',1000
);

insert into account (
    id, name, balance
) values (
    gen_random_uuid(),'peter',500
);

commit;