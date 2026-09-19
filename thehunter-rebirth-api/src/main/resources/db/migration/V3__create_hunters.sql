CREATE TABLE hunters (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,

    handle VARCHAR(20) NOT NULL UNIQUE,

    gender INTEGER NOT NULL,
    face INTEGER NOT NULL,

    profile_picture VARCHAR(512),
    banner_id INTEGER,

    hunter_score INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_hunters_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);