INSERT INTO media_assets (id, file_name, mime_type, size_bytes, storage_key, url, created_by)
VALUES
  (1, 'cover_truyen_thong.jpg', 'image/jpeg', 123456, 'seed/cover_truyen_thong.jpg', 'https://example.com/media/cover_truyen_thong.jpg', 'seed'),
  (2, 'cover_net_tieu_bieu.jpg', 'image/jpeg', 118000, 'seed/cover_net_tieu_bieu.jpg', 'https://example.com/media/cover_net_tieu_bieu.jpg', 'seed'),
  (3, 'cover_lich_su.jpg', 'image/jpeg', 98000, 'seed/cover_lich_su.jpg', 'https://example.com/media/cover_lich_su.jpg', 'seed'),
  (4, 'audio_hanh_khuc_1.mp3', 'audio/mpeg', 3450000, 'seed/audio_hanh_khuc_1.mp3', 'https://example.com/audio/hanh-khuc-1.mp3', 'seed'),
  (5, 'audio_tru_tinh_1.mp3', 'audio/mpeg', 3120000, 'seed/audio_tru_tinh_1.mp3', 'https://example.com/audio/tru-tinh-1.mp3', 'seed'),
  (6, 'audio_tru_tinh_2.mp3', 'audio/mpeg', 2980000, 'seed/audio_tru_tinh_2.mp3', 'https://example.com/audio/tru-tinh-2.mp3', 'seed');

INSERT INTO content_categories (id, type, parent_id, name, slug, description, is_visible, sort_order)
VALUES
  (1, 'TRUYEN_THONG', NULL, 'Truyền thống bộ đội', 'truyen-thong-bo-doi', 'Danh mục gốc', b'1', 1),
  (2, 'NET_TIEU_BIEU', NULL, 'Nét tiêu biểu', 'net-tieu-bieu', 'Danh mục gốc', b'1', 1),
  (3, 'TRUYEN_THONG', 1, 'Lịch sử truyền thống', 'lich-su', 'Tổng quan lịch sử', b'1', 1),
  (4, 'TRUYEN_THONG', 1, 'Gương tiêu biểu', 'guong-tieu-bieu', 'Các cá nhân tiêu biểu', b'1', 2),
  (5, 'TRUYEN_THONG', 1, 'Huấn luyện', 'huan-luyen', 'Công tác huấn luyện', b'1', 3),
  (6, 'NET_TIEU_BIEU', 2, 'Giới thiệu', 'gioi-thieu', 'Thông tin giới thiệu', b'1', 1),
  (7, 'NET_TIEU_BIEU', 2, 'Hoạt động', 'hoat-dong', 'Hoạt động nổi bật', b'1', 2),
  (8, 'NET_TIEU_BIEU', 2, 'Thành tích', 'thanh-tich', 'Thành tích nổi bật', b'1', 3);

INSERT INTO content_items (id, category_id, title, summary, body_html, cover_media_id, is_visible, sort_order, view_count)
VALUES
  (1, 3, 'Lịch sử hình thành', 'Tóm tắt lịch sử hình thành', '<p>Nội dung lịch sử hình thành...</p>', 3, b'1', 1, 12),
  (2, 3, 'Dấu mốc quan trọng', 'Các dấu mốc đáng nhớ', '<p>Nội dung dấu mốc...</p>', 3, b'1', 2, 7),
  (3, 4, 'Gương chiến sĩ A', 'Câu chuyện tiêu biểu', '<p>Nội dung gương chiến sĩ A...</p>', 1, b'1', 1, 3),
  (4, 5, 'Giáo án huấn luyện mẫu', 'Mẫu giáo án', '<p>Nội dung giáo án huấn luyện...</p>', 1, b'1', 1, 2),
  (5, 6, 'Giới thiệu đơn vị', 'Tóm tắt giới thiệu', '<p>Nội dung giới thiệu đơn vị...</p>', 2, b'1', 1, 5),
  (6, 7, 'Hoạt động quý I', 'Tổng hợp hoạt động quý I', '<p>Nội dung hoạt động quý I...</p>', 2, b'1', 1, 4),
  (7, 8, 'Thành tích năm 2025', 'Danh sách thành tích năm 2025', '<p>Nội dung thành tích...</p>', 2, b'1', 1, 6);

INSERT INTO song_categories (id, parent_id, name, slug, description, is_visible, sort_order)
VALUES
  (1, NULL, 'Ca khúc truyền thống', 'ca-khuc-truyen-thong', 'Danh mục gốc', b'1', 1),
  (2, 1, 'Hành khúc', 'hanh-khuc', 'Ca khúc hành khúc', b'1', 1),
  (3, 1, 'Trữ tình', 'tru-tinh', 'Ca khúc trữ tình', b'1', 2),
  (4, 1, 'Thiếu nhi', 'thieu-nhi', 'Ca khúc thiếu nhi', b'1', 3);

INSERT INTO songs (id, category_id, title, lyric, audio_media_id, audio_url, duration_sec, is_visible, sort_order)
VALUES
  (1, 2, 'Bài hành khúc 1', 'Lời bài hát hành khúc...', 4, NULL, 180, b'1', 1),
  (2, 2, 'Bài hành khúc 2', 'Lời bài hát hành khúc 2...', NULL, 'https://example.com/audio/hanh-khuc-2.mp3', 190, b'1', 2),
  (3, 3, 'Bài trữ tình 1', 'Lời bài hát trữ tình...', 5, NULL, 200, b'1', 1),
  (4, 3, 'Bài trữ tình 2', 'Lời bài hát trữ tình 2...', 6, NULL, 210, b'1', 2),
  (5, 4, 'Bài thiếu nhi 1', 'Lời bài hát thiếu nhi...', NULL, 'https://example.com/audio/thieu-nhi-1.mp3', 160, b'1', 1);
