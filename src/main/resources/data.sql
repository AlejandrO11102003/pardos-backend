INSERT INTO products (id, name, category, price, available, image) VALUES
(1,'1/4 Pollo a la Brasa','brasa',18.90, true, NULL),
(2,'1/2 Pollo a la Brasa','brasa',32.90, true, NULL),
(3,'Pollo Entero','brasa',58.90, true, NULL),
(4,'Parrilla Personal','parrilla',25.90, true, NULL),
(5,'Parrilla Familiar','parrilla',85.90, true, NULL),
(6,'Ensalada César','ensaladas',15.90, true, NULL),
(7,'Ensalada Criolla','ensaladas',8.90, true, NULL),
(8,'Mousse de Chocolate','postres',12.90, true, NULL),
(9,'Inca Kola 1.5L','bebidas',7.50, true, NULL),
(10,'Chicha Morada 1L','bebidas',8.90, true, NULL);

-- tables 1..12
INSERT INTO tables (id, number, capacity, status, current_order_id) VALUES
(1,1,2,'available', NULL),(2,2,2,'available', NULL),(3,3,2,'available', NULL),(4,4,2,'available', NULL),
(5,5,4,'available', NULL),(6,6,4,'available', NULL),(7,7,4,'available', NULL),(8,8,4,'available', NULL),
(9,9,6,'available', NULL),(10,10,6,'available', NULL),(11,11,6,'available', NULL),(12,12,6,'available', NULL);
