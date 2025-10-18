-- Create the sample_person table
-- Create the sequence for ID generation
CREATE TABLE IF NOT EXISTS idgenerator (
                                           next_val BIGINT
);

-- Initialize the sequence starting at 1000 (as defined in AbstractEntity)
INSERT INTO idgenerator (next_val)
SELECT 1000
    WHERE NOT EXISTS (SELECT 1 FROM idgenerator);

-- Create the sample_person table
CREATE TABLE IF NOT EXISTS sample_person (
                                             id BIGINT NOT NULL PRIMARY KEY,
                                             version INT NOT NULL,
                                             first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    date_of_birth DATE,
    occupation VARCHAR(255),
    role VARCHAR(255),
    important BOOLEAN DEFAULT FALSE
    );

-- Create the basketball_session table
CREATE TABLE IF NOT EXISTS basketball_session (
                                                  id BIGINT NOT NULL PRIMARY KEY,
                                                  version INT NOT NULL,
                                                  time_minutes DOUBLE,
                                                  shots INT,
                                                  hits INT,
                                                  created_at DATETIME
);

-- Create the skill table (stores basketball skills like crossover, shooting)
CREATE TABLE IF NOT EXISTS skill (
                                     id BIGINT NOT NULL PRIMARY KEY,
                                     version INT NOT NULL,
                                     name VARCHAR(255) NOT NULL,
    description TEXT,
    video_file_name VARCHAR(255)
    );

-- Create the profile table (stores player profiles)
CREATE TABLE IF NOT EXISTS profile (
                                       id BIGINT NOT NULL PRIMARY KEY,
                                       version INT NOT NULL,
                                       name VARCHAR(255) NOT NULL,
    description TEXT
    );

-- Create the junction table for many-to-many relationship between profiles and skills
CREATE TABLE IF NOT EXISTS profile_skill (
                                             profile_id BIGINT NOT NULL,
                                             skill_id BIGINT NOT NULL,
                                             PRIMARY KEY (profile_id, skill_id),
    FOREIGN KEY (profile_id) REFERENCES profile(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
    );