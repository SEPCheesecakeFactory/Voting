CREATE SCHEMA IF NOT EXISTS voting_system;

SET SCHEMA 'voting_system';

CREATE TABLE Poll
(
    id    SERIAL PRIMARY KEY,
    title VARCHAR(60)
);

CREATE TABLE Question
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(180),
    description VARCHAR(250),
    poll_id     INT references Poll (id)
);

CREATE TABLE ChoiceOption
(
    id          SERIAL PRIMARY KEY,
    value       VARCHAR(100),
    question_id INT references Question (id)
);

CREATE TABLE Users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(40)
);

CREATE TABLE VotedChoice
(
    vote_id          INT references Users (id),
    choice_option_id INT references ChoiceOption (id),
    PRIMARY KEY (vote_id, choice_option_id)
);

CREATE TABLE PollOwnership
(
    user_id INT references Users (id),
    poll_id INT references Poll (id),
    PRIMARY KEY (user_id, poll_id)
);

INSERT INTO Poll (title)
VALUES ('Dupa');
INSERT INTO Question (title, description, poll_id)
VALUES ('Question Title', 'Description', 1);
INSERT INTO ChoiceOption (value, question_id)
VALUES ('Yes', 1);
INSERT INTO Users (username)
VALUES ('Wiktor Belzedup');