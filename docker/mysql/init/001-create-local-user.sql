create user if not exists 'naroo'@'localhost' identified by 'naroo';
grant all privileges on `naroo`.* to 'naroo'@'localhost';
flush privileges;
