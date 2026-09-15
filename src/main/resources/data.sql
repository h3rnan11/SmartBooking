-- Sample data loaded on every startup (spring.sql.init.mode=always).
-- All users share the password "password123" (BCrypt hash below).

INSERT IGNORE INTO users (id, name, last_name, email, password, role, id_local) VALUES
    (10, 'Carlos', 'Ramirez', 'carlos.owner@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'OWNER', NULL);

INSERT IGNORE INTO local (id, name, location, id_owner, category) VALUES
    (1, 'Bella Hair Studio', 'Av. Principal 123, Springfield', 1, 'HAIR_DRESSER');

INSERT IGNORE INTO users (id, name, last_name, email, password, role, id_local) VALUES
    (3, 'Lucia', 'Fernandez', 'lucia.employee@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'EMPLOYEE', 1),
    (4, 'Marcos', 'Diaz', 'marcos.employee@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'EMPLOYEE', 1),
    (5, 'Ana', 'Torres', 'ana.client@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'CLIENT', NULL),
    (6, 'Pedro', 'Lopez', 'pedro.client@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'CLIENT', NULL);

-- status has no @Enumerated on the entity, so JPA persists it as the enum ordinal:
-- 0=PENDING, 1=CONFIRMED, 2=CANCELLED, 3=COMPLETED
INSERT IGNORE INTO appointments (id, date, start_time, end_time, status, id_client, id_employee) VALUES
    (1, '2026-09-20', '10:00:00', '10:30:00', 1, 4, 3),
    (2, '2026-09-21', '15:00:00', '15:45:00', 0, 5, 4);
