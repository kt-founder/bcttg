SET @history_root_id := NULL;
SET @history_child_id := NULL;

SELECT id INTO @history_root_id
FROM content_categories
WHERE deleted_at IS NULL
  AND type = 'SO_DO_LICH_SU'
  AND parent_id IS NULL
  AND slug = 'so-do-lich-su'
LIMIT 1;

INSERT INTO content_categories (type, parent_id, name, slug, description, is_visible, sort_order)
SELECT 'SO_DO_LICH_SU', NULL, 'So do lich su', 'so-do-lich-su', 'Danh muc so do lich su don vi', b'1', 1
WHERE @history_root_id IS NULL;

SET @history_root_id := COALESCE(@history_root_id, LAST_INSERT_ID());

SELECT id INTO @history_child_id
FROM content_categories
WHERE deleted_at IS NULL
  AND type = 'SO_DO_LICH_SU'
  AND parent_id = @history_root_id
  AND slug = 'so-do-don-vi'
LIMIT 1;

INSERT INTO content_categories (type, parent_id, name, slug, description, is_visible, sort_order)
SELECT 'SO_DO_LICH_SU', @history_root_id, 'So do don vi', 'so-do-don-vi', 'Cac so do va bai viet lich su theo don vi', b'1', 1
WHERE @history_child_id IS NULL;

SET @history_child_id := COALESCE(@history_child_id, LAST_INSERT_ID());

INSERT INTO content_items (category_id, title, summary, body_html, cover_media_id, is_visible, sort_order, published_at, view_count)
SELECT
  @history_child_id,
  'So do vong tron don vi',
  'Tong hop so do lich su, cau truc va cac moc phat trien cua don vi',
  '<div><h2>So do vong tron don vi</h2><p>Noi dung tong hop theo don vi, cho phep tim kiem, loc va xem chi tiet theo tuyen noi dung lich su.</p><ol><li>Giai doan hinh thanh</li><li>Giai doan cung co</li><li>Giai doan phat trien</li></ol></div>',
  3,
  b'1',
  1,
  UTC_TIMESTAMP(),
  0
WHERE NOT EXISTS (
  SELECT 1
  FROM content_items
  WHERE deleted_at IS NULL
    AND category_id = @history_child_id
    AND title = 'So do vong tron don vi'
);
