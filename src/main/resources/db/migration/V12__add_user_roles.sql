alter table user_accounts
    add column role varchar(32) not null default 'STUDENT';

create index idx_user_accounts_role
    on user_accounts (role);
