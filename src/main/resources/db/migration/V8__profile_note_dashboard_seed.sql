INSERT INTO data_profiles (
  profile_type, full_name, position, unit_name, rank_name, hero_title,
  contact_phone, birth_date, hometown, summary, biography, achievements,
  avatar_media_id, is_visible, sort_order, created_by_phone
)
VALUES
  (
    'THU_TRUONG', 'Nguyen Van Binh', 'Chi huy truong', 'Bo chi huy trung doan', 'Thieu tuong', NULL,
    '0901000001', '1972-05-10', 'Ha Noi',
    'Lanh dao don vi qua nhieu giai doan phat trien.',
    '<p>Dong chi Nguyen Van Binh phu trach cong tac to chuc va dieu hanh toan don vi.</p>',
    '<ul><li>Bang khen cap Bo Quoc phong</li><li>Danh hieu Chien si thi dua toan quan</li></ul>',
    1, b'1', 1, '0900000001'
  ),
  (
    'THU_TRUONG', 'Tran Quoc Nam', 'Pho chi huy truong', 'Phong Tham muu', 'Dai ta', NULL,
    '0901000002', '1976-09-21', 'Nam Dinh',
    'Phu trach cong tac tham muu va huan luyen.',
    '<p>Dong chi Tran Quoc Nam co kinh nghiem trong to chuc huan luyen cap don vi.</p>',
    '<ul><li>Bang khen cap Quan khu</li></ul>',
    2, b'1', 2, '0900000001'
  ),
  (
    'CHIEN_SI', 'Le Van Cuong', 'Trung doi truong', 'Dai doi 1', 'Dai uy', NULL,
    '0901000011', '1988-02-15', 'Thanh Hoa',
    'Can bo tre tieu bieu trong huan luyen va cong tac doan.',
    '<p>Dong chi Le Van Cuong tham gia nhieu phong trao thi dua cap trung doan.</p>',
    '<ul><li>Chien si thi dua co so 3 nam lien tiep</li></ul>',
    3, b'1', 1, '0900000002'
  ),
  (
    'CHIEN_SI', 'Pham Van Son', 'Chinh tri vien', 'Dai doi 2', 'Thuong uy', NULL,
    '0901000012', '1990-11-03', 'Nghe An',
    'Can bo chinh tri co nhieu sang kien trong giao duc truyen thong.',
    '<p>Dong chi Pham Van Son trien khai nhieu mo hinh hoc tap hieu qua.</p>',
    '<ul><li>Bang khen cap Tinh doan</li></ul>',
    NULL, b'1', 2, '0900000002'
  ),
  (
    'ANH_HUNG', 'Vo Thi Huong', 'Nguyen can bo thong tin', 'Don vi thong tin truyen thong', 'Trung ta', 'Anh hung luc luong vu trang',
    '0901000021', '1965-04-28', 'Quang Tri',
    'Guong sang ve tinh than dung cam va sang tao.',
    '<p>Dong chi Vo Thi Huong duoc phong tang danh hieu Anh hung trong thoi ky doi moi.</p>',
    '<ul><li>Huan chuong chien cong hang Nhat</li></ul>',
    NULL, b'1', 1, '0900000001'
  ),
  (
    'ANH_HUNG', 'Doan Minh Tuan', 'Nguyen chi huy dai doi', 'Dai doi dac nhiem', 'Thuong ta', 'Anh hung luc luong vu trang',
    '0901000022', '1968-08-14', 'Thai Binh',
    'Dai dien cho the he can bo chien si xuat sac.',
    '<p>Dong chi Doan Minh Tuan co nhieu dong gop noi bat trong cong tac huan luyen va chien dau.</p>',
    '<ul><li>Huan chuong bao ve To quoc hang Nhi</li></ul>',
    NULL, b'1', 2, '0900000001'
  );

INSERT INTO personal_notes (
  owner_user_id, title, content, color_code, reminder_at, is_pinned, is_archived
)
VALUES
  (
    1,
    'Cap nhat noi dung truyen thong',
    'Can ra soat lai toan bo bai viet loai Truyen thong truoc ngay 10/03.',
    '#FDE68A',
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 3 DAY),
    b'1',
    b'0'
  ),
  (
    1,
    'Kiem tra media upload',
    'Kiem tra dung luong file audio va quy uoc ten file moi.',
    '#BFDBFE',
    NULL,
    b'0',
    b'0'
  ),
  (
    2,
    'Lich hop phong Ke hoach',
    'Hop vao 08:30 thu Hai, thong qua bao cao quy.',
    '#D1FAE5',
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 1 DAY),
    b'1',
    b'0'
  );

INSERT INTO system_audit_logs (
  actor_user_id, actor_name, action_type, module_name, entity_name, detail, status, created_at
)
VALUES
  (1, 'Nguyen Van Binh', 'UPDATE', 'CONTENT', 'Lich su hinh thanh don vi', 'Cap nhat noi dung bai viet', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 MINUTE)),
  (2, 'Tran Quoc Nam', 'CREATE', 'PROFILE', 'Ho so Thieu tuong Tran Van B', 'Them moi ho so du lieu', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 MINUTE)),
  (1, 'Nguyen Van Binh', 'CREATE', 'SONG', 'Hanh khuc Tang Thiet Giap', 'Them bai hat moi vao thu vien', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 HOUR)),
  (4, 'Pham Van D', 'CREATE', 'USER', 'Tai khoan Trung uy Pham Van D', 'Khoi tao tai khoan nguoi dung', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 HOUR)),
  (1, 'Nguyen Van Binh', 'UPDATE', 'SYSTEM', 'Thiet lap bao mat', 'Cap nhat cau hinh bao mat he thong', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 HOUR)),
  (3, 'Le Thi Mai', 'VIEW', 'CONTENT', 'Lich su hinh thanh don vi', 'Truy cap noi dung cong khai', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 DAY)),
  (3, 'Le Thi Mai', 'VIEW', 'CONTENT', 'Dau moc quan trong', 'Truy cap noi dung cong khai', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY)),
  (5, 'Do Thi Lan', 'VIEW', 'SONG', 'Hanh khuc don vi', 'Nghe ca khuc trong thu vien', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 DAY)),
  (6, 'Hoang Van Nam', 'VIEW', 'PROFILE', 'Nguyen Van Binh', 'Xem ho so thu truong', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 4 DAY)),
  (2, 'Tran Quoc Nam', 'VIEW', 'SONG', 'Khuc hat que huong', 'Truy cap bai hat cong khai', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY)),
  (4, 'Pham Van D', 'VIEW', 'CONTENT', 'Thanh tich nam 2025', 'Xem noi dung tren mobile', 'SUCCESS', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 6 DAY));
