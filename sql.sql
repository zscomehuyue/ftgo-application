create user 'order'@'*' identified by 'root';
grant all privileges  on *.*  to "order"@'%' identified by 'root';

create user 'account'@'*' identified by 'root';
grant all privileges  on *.*  to "account"@'%' identified by 'root';

create user 'consumer'@'*' identified by 'root';
grant all privileges  on *.*  to "consumer"@'%' identified by 'root';

create user 'kitchen'@'*' identified by 'root';
grant all privileges  on *.*  to "kitchen"@'%' identified by 'root';

create user 'restaurant'@'*' identified by 'root';
grant all privileges  on *.*  to "restaurant"@'%' identified by 'root';

create user 'eventuate'@'*' identified by 'root';
grant all privileges  on *.*  to "eventuate"@'%' identified by 'root';

CREATE DATABASE IF NOT EXISTS ftgo_accounting_service DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS ftgo_order_service DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS ftgo_kitchen_service DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS eventuate DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci;
