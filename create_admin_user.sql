-- Create Admin User for WoodToy Backend
-- Password: Admin123! (hashed with BCrypt)

USE vietmythluminarts_db;

-- Insert admin user
INSERT INTO users (
    name, 
    email, 
    password, 
    role, 
    provider, 
    is_active, 
    last_login, 
    created_at, 
    updated_at
) VALUES (
    'Admin User',
    'admin@woodtoy.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: Admin123!
    'admin',
    'local',
    1,
    NOW(),
    NOW(),
    NOW()
);

-- Verify the user was created
SELECT id, name, email, role, is_active, created_at 
FROM users 
WHERE email = 'admin@woodtoy.com';
