-- 清理已有数据
DELETE FROM users;

-- 重置自增ID
ALTER TABLE users AUTO_INCREMENT = 1;

-- 插入测试数据
INSERT INTO users (username, password, email, phone, real_name, role, enabled, create_time, update_time)
VALUES 
('admin', '$2a$10$rDkPvvAFV6GgAgzw1pzgDeQB3YVQMWrGgPqi9jUEHhs3k1UF3PtIi', 'admin@test.com', '13800000000', 'Admin User', 'ADMIN', 1, NOW(), NOW()),
('user1', '$2a$10$rDkPvvAFV6GgAgzw1pzgDeQB3YVQMWrGgPqi9jUEHhs3k1UF3PtIi', 'user1@test.com', '13800000001', 'Test User 1', 'USER', 1, NOW(), NOW()),
('user2', '$2a$10$rDkPvvAFV6GgAgzw1pzgDeQB3YVQMWrGgPqi9jUEHhs3k1UF3PtIi', 'user2@test.com', '13800000002', 'Test User 2', 'USER', 1, NOW(), NOW()),
('staff1', '$2a$10$rDkPvvAFV6GgAgzw1pzgDeQB3YVQMWrGgPqi9jUEHhs3k1UF3PtIi', 'staff1@test.com', '13800000003', 'Staff User 1', 'STAFF', 1, NOW(), NOW()); 