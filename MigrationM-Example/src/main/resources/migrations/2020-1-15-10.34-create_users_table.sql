CREATE TABLE users (
    uuid VARCHAr(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tickets INTEGER
);
insert INTO users (uuid, name, tickets) VALUES ("94f25426-5737-11eb-ae93-0242ac130002", "Tom", 0);
insert INTO users (uuid, name, tickets) VALUES ("94f256e2-5737-11eb-ae93-0242ac130002", "Jerry", 14);
insert INTO users (uuid, name, tickets) VALUES ("94f25854-5737-11eb-ae93-0242ac130002", "MartenM", 54);