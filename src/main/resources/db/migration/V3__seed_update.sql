INSERT INTO media_assets (file_name, mime_type, size_bytes, storage_key, url, created_by)
VALUES
  ('cover_hoat_dong.jpg', 'image/jpeg', 110000, 'seed/cover_hoat_dong.jpg', 'https://example.com/media/cover_hoat_dong.jpg', 'seed'),
  ('audio_hanh_khuc_3.mp3', 'audio/mpeg', 3300000, 'seed/audio_hanh_khuc_3.mp3', 'https://example.com/audio/hanh-khuc-3.mp3', 'seed');

INSERT INTO content_categories (type, parent_id, name, slug, description, is_visible, sort_order)
VALUES
  ('TRUYEN_THONG', 1, 'Kỷ niệm', 'ky-niem', 'Các hoạt động kỷ niệm', b'1', 4),
  ('NET_TIEU_BIEU', 2, 'Đối ngoại', 'doi-ngoai', 'Hoạt động đối ngoại', b'1', 4);

INSERT INTO content_items (category_id, title, summary, body_html, cover_media_id, is_visible, sort_order, view_count)
VALUES
  (7, 'Hoạt động quý II', 'Tổng hợp hoạt động quý II', '<p>Nội dung hoạt động quý II...</p>', NULL, b'1', 2, 1),
  (8, 'Thành tích năm 2026', 'Danh sách thành tích năm 2026', '<p>Nội dung thành tích 2026...</p>', NULL, b'1', 2, 0);

INSERT INTO songs (category_id, title, lyric, audio_media_id, audio_url, duration_sec, is_visible, sort_order)
VALUES
  (2, 'Bài hành khúc 3', 'Lời bài hát hành khúc 3...', NULL, 'https://example.com/audio/hanh-khuc-3.mp3', 195, b'1', 3);
