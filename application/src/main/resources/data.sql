--nextval('HIBERNATE_SEQUENCE')

INSERT INTO PUBLIC.CATEGORY (ID, DESCRIPTION, NAME) VALUES (nextval('HIBERNATE_SEQUENCE'), 'Component Collection', 'Component Collection');
INSERT INTO PUBLIC.CATEGORY (ID, DESCRIPTION, NAME) VALUES (nextval('HIBERNATE_SEQUENCE'), 'PBC', 'PBC');
INSERT INTO PUBLIC.CATEGORY (ID, DESCRIPTION, NAME) VALUES (nextval('HIBERNATE_SEQUENCE'), 'Solution Template', 'Solution Template');

insert into ORGANISATION (ID, DESCRIPTION, NAME) VALUES (nextval('HIBERNATE_SEQUENCE'), 'Entando inc.', 'Entando inc.');
insert into ORGANISATION (ID, DESCRIPTION, NAME) VALUES (nextval('HIBERNATE_SEQUENCE'), 'Solving Team s.r.l', 'Solving Team');