INSERT INTO content_categories (id, type, parent_id, name, slug, description, is_visible, sort_order)
VALUES
  (1, 'TRUYEN_THONG', NULL, 'Truyền thống bộ đội', 'truyen-thong-bo-doi', 'Danh mục gốc', b'1', 1),
  (2, 'NET_TIEU_BIEU', NULL, 'Nét tiêu biểu', 'net-tieu-bieu', 'Danh mục gốc', b'1', 1),
  (3, 'TRUYEN_THONG', 1, 'Lịch sử truyền thống', 'lich-su', 'Tổng quan lịch sử', b'1', 1),
  (4, 'TRUYEN_THONG', 1, 'Gương tiêu biểu', 'guong-tieu-bieu', 'Các cá nhân tiêu biểu', b'1', 2),
  (5, 'NET_TIEU_BIEU', 2, 'Giới thiệu', 'gioi-thieu', 'Thông tin giới thiệu', b'1', 1),
  (6, 'NET_TIEU_BIEU', 2, 'Hoạt động', 'hoat-dong', 'Hoạt động nổi bật', b'1', 2);

INSERT INTO content_items (id, category_id, title, summary, body_html, is_visible, sort_order, view_count)
VALUES
  (1, 3, 'Lịch sử hình thành', 'Tóm tắt lịch sử hình thành', '<p>Nội dung lịch sử hình thành...</p>', b'1', 1, 12),
  (2, 5, 'Giới thiệu đơn vị', 'Tóm tắt giới thiệu', '<p>Nội dung giới thiệu đơn vị...</p>', b'1', 1, 5);

INSERT INTO song_categories (id, parent_id, name, slug, description, is_visible, sort_order)
VALUES
  (1, NULL, 'Ca khúc truyền thống', 'ca-khuc-truyen-thong', 'Danh mục gốc', b'1', 1),
  (2, 1, 'Hành khúc', 'hanh-khuc', 'Ca khúc hành khúc', b'1', 1),
  (3, 1, 'Trữ tình', 'tru-tinh', 'Ca khúc trữ tình', b'1', 2);

INSERT INTO songs (id, category_id, title, lyric, audio_url, duration_sec, is_visible, sort_order)
VALUES
  (1, 2, 'Bài hành khúc 1', 'Lời bài hát hành khúc...', 'https://example.com/audio/hanh-khuc-1.mp3', 180, b'1', 1),
  (2, 3, 'Bài trữ tình 1', 'Lời bài hát trữ tình...', 'https://example.com/audio/tru-tinh-1.mp3', 200, b'1', 1);
