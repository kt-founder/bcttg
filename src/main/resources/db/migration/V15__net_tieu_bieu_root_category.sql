INSERT INTO content_categories (type, parent_id, name, slug, description, is_visible, sort_order, created_at, updated_at)
SELECT 'NET_TIEU_BIEU', NULL, 'Nét tiêu biểu', 'net-tieu-bieu', 'Danh mục gốc module Nét tiêu biểu', b'1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM content_categories
  WHERE deleted_at IS NULL
    AND type = 'NET_TIEU_BIEU'
    AND slug = 'net-tieu-bieu'
    AND parent_id IS NULL
);
