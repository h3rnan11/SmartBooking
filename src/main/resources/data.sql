-- Sample data loaded on every startup (spring.sql.init.mode=always).
-- All users share the password "password123" (BCrypt hash below).

INSERT IGNORE INTO users (id, name, last_name, email, password, role, id_local) VALUES
    (10, 'Carlos', 'Ramirez', 'carlos.owner@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'OWNER', NULL);

INSERT IGNORE INTO local (id, name, location, id_owner, category) VALUES
    (1, 'Bella Hair Studio', 'Av. Principal 123, Springfield', 10, 'HAIR_DRESSER');

INSERT IGNORE INTO users (id, name, last_name, email, password, role, id_local) VALUES
    (3, 'Lucia', 'Fernandez', 'lucia.employee@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'EMPLOYEE', 1),
    (4, 'Marcos', 'Diaz', 'marcos.employee@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'EMPLOYEE', 1),
    (5, 'Ana', 'Torres', 'ana.client@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'CLIENT', NULL),
    (6, 'Pedro', 'Lopez', 'pedro.client@smartbooking.com', '$2a$10$fZp7jLgqTZ635JiRlPuvgOmynWh9LJK.hUxZFBYUnQ.KVFoc7HTx6', 'CLIENT', NULL);


INSERT IGNORE INTO appointments (id, date, start_time, end_time, status, id_client, id_employee) VALUES
    (1, '2026-11-20', '10:00:00', '10:30:00', 'COMPLETED', 6, 3),
    (2, '2026-09-21', '15:00:00', '15:45:00', 'COMPLETED', 6, 4),
    (3, '2026-11-22', '09:30:00', '10:00:00', 'CONFIRMED', 5, 3),
    (4, '2026-09-23', '11:00:00', '11:30:00', 'PENDING', 6, 4),
    (5, '2026-09-24', '16:00:00', '16:45:00', 'CONFIRMED', 5, 4),
    (6, '2026-11-26', '12:00:00', '12:30:00', 'PENDING', 6, 3),
    (7, '2026-11-18', '09:00:00', '09:30:00', 'CANCELLED', 5, 3);


INSERT IGNORE INTO services (id, name, duration_minutes, price, id_local) VALUES
    (1, 'Corte de pelo', 30, 15.00, 1),
    (2, 'Corte y peinado', 45, 25.00, 1);

-- Link sample appointments to a service without overwriting links set later.
UPDATE appointments SET id_service = 1 WHERE id IN (1, 3, 4, 6, 7) AND id_service IS NULL;
UPDATE appointments SET id_service = 2 WHERE id IN (2, 5) AND id_service IS NULL;
