-- Migration for creating users table
CREATE TABLE public.users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    time_zone VARCHAR(255) NOT NULL,
    activated BOOLEAN,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    enabled BOOLEAN,
    department VARCHAR(255),
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating administrators table
CREATE TABLE public.administrators (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating boardrooms table
CREATE TABLE public.boardrooms (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    internet_enabled BOOLEAN NOT NULL,
    "locked" BOOLEAN NOT NULL,
    administrator_id BIGINT UNIQUE REFERENCES public.users(id),
    description VARCHAR(1000) NOT NULL,
    centre VARCHAR(255),
    department VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    identifier VARCHAR(255) UNIQUE,
    meeting_type_supported VARCHAR(255),
    picture VARCHAR(255),
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating boardroom_contact table
CREATE TABLE public.boardroom_contact (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    boardroom_id BIGINT REFERENCES public.boardrooms(id),
    communication_method VARCHAR(255) CHECK (communication_method IN ('PHONE_EXTENSION', 'MOBILE')),
    contact VARCHAR(255),
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating equipments table
CREATE TABLE public.equipments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    boardroom_id BIGINT REFERENCES public.boardrooms(id),
    title VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    model_number VARCHAR(255),
    description VARCHAR(1000),
    picture VARCHAR(255),
    video_url VARCHAR(255),
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    disposed BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating locked_boardrooms table
CREATE TABLE public.locked_boardrooms (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    boardroom_id BIGINT REFERENCES public.boardrooms(id),
    given_reason VARCHAR(255),
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    "locked" BOOLEAN,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating reservations table
CREATE TABLE public.reservations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    boardroom_id BIGINT REFERENCES public.boardrooms(id),
    user_id BIGINT REFERENCES public.users(id),
    meeting_title VARCHAR(1000) NOT NULL,
    meeting_description VARCHAR(2000) NOT NULL,
    meeting_link VARCHAR(255),
    attendees VARCHAR(255) NOT NULL,
    meeting_type VARCHAR(255) CHECK (meeting_type IN ('PHYSICAL', 'HYBRID')),
    approval_status VARCHAR(255) CHECK (approval_status IN ('PENDING', 'APPROVED', 'DECLINED')),
    ict_support_required BOOLEAN NOT NULL,
    is_urgent_meeting BOOLEAN NOT NULL,
    record_meeting BOOLEAN NOT NULL,
    start_date_time TIMESTAMP(6),
    end_date_time TIMESTAMP(6),
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);

-- Migration for creating roles table
CREATE TABLE public.roles (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES public.users(id),
    authority VARCHAR(255) CHECK (authority IN ('USER', 'ADMIN')),
    identifier VARCHAR(255) UNIQUE,
    tag VARCHAR(255) NOT NULL UNIQUE,
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    created_by BIGINT,
    occurred_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    updated_by BIGINT
);
