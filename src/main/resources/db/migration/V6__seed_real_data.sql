UPDATE media_assets
SET
  file_name = 'cover_truyen_thong.jpg',
  url = 'https://media.bcttg.local/seed/cover_truyen_thong.jpg'
WHERE id = 1;

UPDATE media_assets
SET
  file_name = 'cover_net_tieu_bieu.jpg',
  url = 'https://media.bcttg.local/seed/cover_net_tieu_bieu.jpg'
WHERE id = 2;

UPDATE media_assets
SET
  file_name = 'cover_lich_su.jpg',
  url = 'https://media.bcttg.local/seed/cover_lich_su.jpg'
WHERE id = 3;

UPDATE content_categories
SET name = 'Truyen thong bo doi',
    description = 'Danh muc truyen thong cua don vi'
WHERE id = 1;

UPDATE content_categories
SET name = 'Net tieu bieu',
    description = 'Danh muc net tieu bieu cua don vi'
WHERE id = 2;

UPDATE content_categories
SET name = 'Lich su truyen thong',
    description = 'Tom tat lich su hinh thanh va phat trien'
WHERE id = 3;

UPDATE content_categories
SET name = 'Guong tieu bieu',
    description = 'Ca nhan va tap the tieu bieu'
WHERE id = 4;

UPDATE content_categories
SET name = 'Huan luyen',
    description = 'Cong tac huan luyen va boi duong'
WHERE id = 5;

UPDATE content_categories
SET name = 'Gioi thieu',
    description = 'Thong tin tong quan'
WHERE id = 6;

UPDATE content_categories
SET name = 'Hoat dong',
    description = 'Hoat dong noi bat theo quy'
WHERE id = 7;

UPDATE content_categories
SET name = 'Thanh tich',
    description = 'Thanh tich va giai thuong'
WHERE id = 8;

UPDATE content_items
SET title = 'Lich su hinh thanh don vi',
    summary = 'Tom tat qua trinh hinh thanh va phat trien',
    body_html = '<p>Don vi duoc thanh lap nam 1995, trai qua nhieu giai doan phat trien, hoan thanh tot nhiem vu duoc giao.</p>'
WHERE id = 1;

UPDATE content_items
SET title = 'Cac dau moc quan trong',
    summary = 'Nhung dau moc tieu bieu trong qua trinh phat trien',
    body_html = '<p>Nam 2005: mo rong pham vi hoat dong. Nam 2015: nang cap trang thiet bi. Nam 2024: dat chuan thi dua.</p>'
WHERE id = 2;

UPDATE content_items
SET title = 'Guong chien si tieu bieu',
    summary = 'Tap the va ca nhan tieu bieu nam 2025',
    body_html = '<p>Tap the A va dong chi B duoc bieu duong vi thanh tich xuat sac trong phong trao thi dua.</p>'
WHERE id = 3;

UPDATE content_items
SET title = 'Giao an huan luyen mau',
    summary = 'Bo giao an mau cho cong tac huan luyen',
    body_html = '<p>Giao an gom cac noi dung co ban va nang cao, phu hop voi thuc tien don vi.</p>'
WHERE id = 4;

UPDATE content_items
SET title = 'Gioi thieu don vi',
    summary = 'Tong quan ve co cau va nhiem vu',
    body_html = '<p>Don vi thuc hien nhiem vu tuyen truyen, huan luyen va phuc vu cong tac giao duc truyen thong.</p>'
WHERE id = 5;

UPDATE content_items
SET title = 'Hoat dong quy I',
    summary = 'Tong hop hoat dong quy I',
    body_html = '<p>To chuc chuoi sinh hoat chuyen de, giao luu van hoa va cac dot huan luyen tap trung.</p>'
WHERE id = 6;

UPDATE content_items
SET title = 'Thanh tich nam 2025',
    summary = 'Danh sach thanh tich noi bat',
    body_html = '<p>Don vi dat giai nhat thi dua cap co so va nhan bang khen cap thanh pho.</p>'
WHERE id = 7;

UPDATE song_categories
SET name = 'Ca khuc truyen thong',
    description = 'Danh muc ca khuc truyen thong'
WHERE id = 1;

UPDATE song_categories
SET name = 'Hanh khuc',
    description = 'Ca khuc hanh khuc'
WHERE id = 2;

UPDATE song_categories
SET name = 'Tru tinh',
    description = 'Ca khuc tru tinh'
WHERE id = 3;

UPDATE song_categories
SET name = 'Thieu nhi',
    description = 'Ca khuc thieu nhi'
WHERE id = 4;

UPDATE songs
SET title = 'Hanh khuc don vi',
    lyric = 'Nhung buoc chan vung vang, dong long vi nhiem vu chung...'
WHERE id = 1;

UPDATE songs
SET title = 'Hanh khuc xanh',
    lyric = 'Anh em ta len duong, giu vung niem tin...'
WHERE id = 2;

UPDATE songs
SET title = 'Khuc hat que huong',
    lyric = 'Que huong oi, trong tim ta mai nho...'
WHERE id = 3;

UPDATE songs
SET title = 'Tieu doi tre',
    lyric = 'Tieng hat trong treo, uoc mo bay xa...'
WHERE id = 4;

UPDATE songs
SET title = 'Mua he tuoi dep',
    lyric = 'Ngay he den, bao niem vui ruc ro...'
WHERE id = 5;
