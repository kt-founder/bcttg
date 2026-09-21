INSERT INTO user_accounts (id, phone, password_hash, role, is_active)
VALUES
  (1, '0900000001', '{noop}Admin@2026', 'ADMIN', b'1'),
  (2, '0900000002', '{noop}Manager@2026', 'MANAGER', b'1'),
  (3, '0900000003', '{noop}User@2026', 'USER', b'1'),
  (4, '0900000004', '{noop}User@2026', 'USER', b'1'),
  (5, '0900000005', '{noop}User@2026', 'USER', b'1'),
  (6, '0900000006', '{noop}User@2026', 'USER', b'1');

INSERT INTO user_profiles (user_id, full_name, position, unit_name, rank_name, email, address, birth_date)
VALUES
  (1, 'Nguyen Minh Duc', 'Giam doc', 'Phong Tong hop', 'Dai ta', 'duc.nguyen@bcttg.local', '123 Nguyen Trai, Ha Noi', '1979-04-12'),
  (2, 'Tran Quang Huy', 'Pho giam doc', 'Phong Ke hoach', 'Thuong ta', 'huy.tran@bcttg.local', '45 Le Duan, Ha Noi', '1983-11-03'),
  (3, 'Le Thi Mai', 'Chuyen vien', 'Phong Nghiep vu', 'Dai uy', 'mai.le@bcttg.local', '72 Pho Hue, Ha Noi', '1992-02-18'),
  (4, 'Pham Tuan Anh', 'Ky su', 'Phong Cong nghe', 'Thieu uy', 'anh.pham@bcttg.local', '8 Phan Dinh Phung, Ha Noi', '1995-07-29'),
  (5, 'Do Thi Lan', 'Ke toan', 'Phong Tai chinh', 'Trung uy', 'lan.do@bcttg.local', '16 Tran Hung Dao, Hai Phong', '1990-09-14'),
  (6, 'Hoang Van Nam', 'Van thu', 'Phong Hanh chinh', 'Thieu uy', 'nam.hoang@bcttg.local', '220 Le Loi, Da Nang', '1991-12-01');
