alter table user_accounts
    add column email varchar(254) not null;

alter table user_accounts
    add column email_verified boolean not null default false;

alter table user_accounts
    add constraint uk_user_accounts_email unique (email);
