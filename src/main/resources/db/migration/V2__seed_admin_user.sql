-- V2__seed_admin_user.sql
-- Create a default admin account.
-- Password: 'admin123' (BCrypt-hashed)
-- IMPORTANT: Change this password immediately in production!

INSERT INTO agencies (
    name, owner_name, email, password_hash, phone, city,
    role, plan_type, monthly_call_limit, calls_used_this_month,
    setup_completed, active, created_at, updated_at
) VALUES (
    'Platform Admin',
    'Super Admin',
    'admin@gasdsc.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhTy',
    '0000000000',
    'System',
    'ADMIN',
    'ENTERPRISE',
    2147483647,
    0,
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
