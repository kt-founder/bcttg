-- Module Tin tức trở thành nhóm nội dung CMS phẳng (1 cấp) như Nét tiêu biểu.

-- 1) Seed danh mục gốc phẳng cho TIN_TUC.
INSERT INTO content_categories (type, parent_id, name, slug, description, is_visible, sort_order, created_at, updated_at)
SELECT 'TIN_TUC', NULL, 'Tin tức', 'tin-tuc', 'Danh mục gốc module Tin tức', b'1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM content_categories
  WHERE deleted_at IS NULL
    AND type = 'TIN_TUC'
    AND slug = 'tin-tuc'
    AND parent_id IS NULL
);

-- 2) Gỡ module 'ghi-chu' thêm tay ngoài thiết kế (không có trong enum HomeModuleId,
--    không có nguồn dữ liệu công khai — ghi chú là dữ liệu cá nhân của từng user).
DELETE FROM home_modules WHERE id = 'ghi-chu';

-- 3) Cho khách vãng lai thấy module tin-tuc (thế chỗ ghi-chu trên trang chủ).
UPDATE home_modules SET is_guest = b'1' WHERE id = 'tin-tuc';
