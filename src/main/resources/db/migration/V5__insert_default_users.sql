-- Insertar usuarios por defecto para testing
INSERT INTO users (id, public_id, username, email, password_hash, role, created_at, version) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440001', 
    'USR001', 
    'admin', 
    'admin@example.com', 
    'admin123', -- password sin hash para testing
    'ADMIN', 
    CURRENT_TIMESTAMP,
    0
),
(
    '550e8400-e29b-41d4-a716-446655440002', 
    'USR002', 
    'user', 
    'user@example.com', 
    'user123', -- password sin hash para testing
    'USER', 
    CURRENT_TIMESTAMP,
    0
);