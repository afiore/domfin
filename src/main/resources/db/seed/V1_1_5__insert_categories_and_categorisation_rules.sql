INSERT INTO categories (id, label)
VALUES ('ELECTRICITY', 'Electricity'),
       ('WATER', 'Water'),
       ('HEATING', 'Heating'),
       ('INTERNET', 'Internet and Mobile services'),
       ('CAR', 'Car'),
       ('HEALTH', 'Health Care'),
       ('GROCERIES', 'Food and Groceries'),
       ('ONLINE', 'Online service subscriptions'),
       ('HOME', 'Home and furniture'),
       ('WORK', 'Workspace services'),
       ('TRAVEL', 'Holidays and Travel'),
       ('ACTIVITIES', 'Leisure and activities'),
       ('SHOPPING', 'Misc Shopping');

INSERT into categorisation_rules (category_id, substring)
VALUES ('INTERNET', 'TELEFONICA DE ESPANA'),
       ('INTERNET', 'N26'),
       ('INTERNET', 'GOOGLE'),
       ('INTERNET', 'SpotifyES'),
       ('GROCERIES', 'ALDI'),
       ('GROCERIES', 'ESPIGA'),
       ('GROCERIES', 'VERITAS'),
       ('GROCERIES', 'DIA'),
       ('GROCERIES', 'BONPREU'),
       ('GROCERIES', 'BON AREA'),
       ('GROCERIES', 'FORNSENRICH'),
       ('GROCERIES', 'CASA AMETLLER'),
       ('TRAVEL', 'KIWI.COM'),
       ('TRAVEL', 'ETRAVELI'),
       ('WORK', 'CAHOOT COWORKING'),
       ('CAR', 'AUCAT'),
       ('CAR', 'VNG APARCAMENTS'),
       ('CAR', 'PARKING'),
       ('SHOPPING', 'AMZN Mktp');
