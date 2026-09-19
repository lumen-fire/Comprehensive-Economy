CREATE TABLE IF NOT EXISTS homes (
            uuid TEXT NOT NULL,
            name TEXT NOT NULL,
            world TEXT NOT NULL,
            x DOUBLE NOT NULL,
            y DOUBLE NOT NULL,
            z DOUBLE NOT NULL,
            yaw FLOAT NOT NULL,
            pitch FLOAT NOT NULL,
            PRIMARY KEY (uuid, name)
            )