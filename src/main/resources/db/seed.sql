-- RublinMart Initial Seed Data
-- Demo accounts use the documented passwords from the README.md:
-- Admin / Seller / Buyer accounts all use Password@123
-- BCrypt hash generated for that password: $2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K

MERGE INTO users (id, name, email, password_hash, role) KEY(id) VALUES 
(1, 'System Admin', 'admin@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'ADMIN'),
(2, 'Tech Emporium', 'seller1@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'SELLER'),
(3, 'Style Studio', 'seller2@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'SELLER'),
(4, 'John Doe', 'buyer1@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'BUYER'),
(5, 'Jane Smith', 'buyer2@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'BUYER'),
(6, 'Robert Johnson', 'buyer3@rublinmart.com', '$2a$10$1KFDDCFIbl2E96TQ1V5K5O5QbfVGm6.NTfyaGwaXotoLFtekEVG9K', 'BUYER');

MERGE INTO products (id, seller_id, name, description, price, stock_qty, category, image_url) KEY(id) VALUES
(1, 2, 'ProBook Laptop 15"', 'High-performance laptop with 16GB RAM and 512GB SSD for power users.', 1299.99, 15, 'Electronics', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500'),
(2, 2, 'Wireless Noise-Canceling Headphones', 'Premium sound clarity with active noise cancellation and 30-hour battery life.', 249.50, 30, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500'),
(3, 2, 'Smart Fitness Watch', 'Track heart rate, workouts, sleep, and GPS activity with vibrant AMOLED display.', 199.99, 25, 'Electronics', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500'),
(4, 2, 'Ultra HD 4K Monitor 27"', 'Crisp color accuracy, 144Hz refresh rate, HDR support for gaming and design.', 349.99, 10, 'Electronics', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500'),
(5, 3, 'Classic Leather Jacket', '100% genuine lambskin leather jacket with premium stitching and inner lining.', 189.99, 12, 'Fashion', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500'),
(6, 3, 'Urban Running Sneakers', 'Lightweight breathable sneakers designed for comfort and maximum endurance.', 89.99, 40, 'Fashion', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500'),
(7, 3, 'Ergonomic Desk Chair', 'Adjustable lumbar support, breathable mesh back, and smooth-rolling casters.', 220.00, 8, 'Home & Office', 'https://images.unsplash.com/photo-1580481072645-022f9a6d83d0?w=500'),
(8, 3, 'Stainless Steel Thermal Flask 1L', 'Keeps drinks ice cold for 24h or steaming hot for 12h. Eco-friendly design.', 29.99, 50, 'Home & Office', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500');

MERGE INTO orders (id, buyer_id, status, total_amount) KEY(id) VALUES
(1, 4, 'DELIVERED', 1549.49),
(2, 5, 'SHIPPED', 89.99);

MERGE INTO order_items (id, order_id, product_id, quantity, unit_price) KEY(id) VALUES
(1, 1, 1, 1, 1299.99),
(2, 1, 2, 1, 249.50),
(3, 2, 6, 1, 89.99);

MERGE INTO reviews (id, product_id, user_id, rating, comment) KEY(id) VALUES
(1, 1, 4, 5, 'Exceeded my expectations! Fast boot times and great build quality.'),
(2, 2, 4, 4, 'Noise cancellation is excellent. Slightly snug fit but overall great sound.'),
(3, 6, 5, 5, 'Super comfortable for everyday runs and walking.');
