-- Seed default admin user
-- Password: Admin@123 (hashed with BCrypt)
INSERT INTO users (email, password_hash, name, role) VALUES
('admin@nexusmart.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'ROLE_ADMIN');

-- Seed sample products across categories
INSERT INTO products (name, description, price, stock, category, image_url) VALUES
('Wireless Bluetooth Headphones', 'Premium noise-cancelling wireless headphones with 30-hour battery life, ultra-comfortable ear cushions, and crystal-clear audio quality. Perfect for music lovers and professionals alike.', 149.99, 50, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500'),
('Smart Watch Pro', 'Advanced smartwatch with health monitoring, GPS tracking, heart rate sensor, and 7-day battery life. Water-resistant up to 50 meters.', 299.99, 30, 'Electronics', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500'),
('Laptop Stand Aluminum', 'Ergonomic aluminum laptop stand with adjustable height and angle. Improves posture and reduces neck strain during long work sessions.', 79.99, 100, 'Accessories', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500'),
('Mechanical Keyboard RGB', 'Premium mechanical keyboard with customizable RGB backlighting, Cherry MX switches, and programmable macro keys. Built for gamers and typists.', 129.99, 45, 'Electronics', 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=500'),
('Wireless Mouse Ergonomic', 'Ergonomic wireless mouse with precision tracking, silent click buttons, and rechargeable battery. Designed for comfort during extended use.', 49.99, 80, 'Accessories', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500'),
('USB-C Hub 7-in-1', 'Versatile USB-C hub with HDMI, USB 3.0 ports, SD card reader, and power delivery. Perfect for laptops with limited ports.', 39.99, 120, 'Accessories', 'https://images.unsplash.com/photo-1625948515291-69613efd103f?w=500'),
('Portable SSD 1TB', 'Ultra-fast portable SSD with 1TB storage, USB 3.2 Gen 2, and transfer speeds up to 1050MB/s. Compact and rugged design.', 159.99, 25, 'Storage', 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=500'),
('Webcam 4K Ultra HD', 'Professional 4K webcam with auto-focus, noise-canceling microphone, and low-light correction. Ideal for streaming and video conferencing.', 89.99, 60, 'Electronics', 'https://images.unsplash.com/photo-1587826080692-f439cd0b70da?w=500'),
('Desk Lamp LED', 'Adjustable LED desk lamp with multiple brightness levels, color temperature control, and USB charging port. Modern minimalist design.', 34.99, 150, 'Home', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500'),
('Monitor Arm Dual', 'Heavy-duty dual monitor arm with full motion adjustment, gas spring mechanism, and integrated cable management. Supports up to 27-inch monitors.', 119.99, 40, 'Accessories', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500'),
('Wireless Charger Pad', 'Fast wireless charging pad compatible with all Qi-enabled devices. 15W fast charging with LED indicator and foreign object detection.', 29.99, 200, 'Accessories', 'https://images.unsplash.com/photo-1586816879360-004f5b0c51e5?w=500'),
('Bluetooth Speaker Portable', 'Waterproof portable Bluetooth speaker with 360-degree sound, 20-hour battery life, and built-in microphone for hands-free calls.', 69.99, 75, 'Electronics', 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500'),
('Phone Stand Adjustable', 'Universal phone stand with adjustable angle and height. Compatible with all smartphones and tablets. Non-slip base.', 19.99, 180, 'Accessories', 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=500'),
('Cable Management Kit', 'Complete cable management kit with velcro ties, cable clips, and organizer box. Keep your workspace neat and organized.', 24.99, 220, 'Accessories', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=500'),
('Screen Cleaning Kit', 'Professional screen cleaning kit with microfiber cloth and alcohol-free solution. Safe for all screens including laptops, phones, and tablets.', 14.99, 300, 'Accessories', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500');
