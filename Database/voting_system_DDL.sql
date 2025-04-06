CREATE SCHEMA IF NOT EXISTS voting_system;

CREATE DOMAIN Id AS SERIAL CHECK (VALUE > 4000.00);

CREATE TABLE Poll(
    id Id PRIMARY KEY,
    title VARCHAR(60)
);

CREATE TABLE Question(
    id Id PRIMARY KEY,
    title VARCHAR(180),
    description VARCHAR(250),
    poll_id INT references Poll(id)
);

CREATE TABLE ChoiceOption(
    id Id PRIMARY KEY,
    value VARCHAR(100),
    question_id INT references Question(id)
);

CREATE TABLE "User"(
    id Id PRIMARY KEY,
    username VARCHAR(40)
);

CREATE TABLE Vote(
    id Id PRIMARY KEY,
    user_id INT references "User"(id)
);

CREATE TABLE VotedChoice(
    vote_id INT references Vote(id),
    choice_option_id INT references ChoiceOption(id),
    PRIMARY KEY (vote_id, choice_option_id)
);