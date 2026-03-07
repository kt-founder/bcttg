INSERT INTO system_settings (
  system_name, system_description, timezone, language, records_per_page, show_avatar, compact_mode,
  password_min_length, require_uppercase, require_number, require_special_char, session_timeout,
  max_login_attempts, require_2fa, smtp_host, smtp_port, smtp_user, smtp_pass, email_from,
  notify_new_login, notify_pending_content, notify_security_alerts, notify_periodic_reports,
  auto_backup_enabled, backup_frequency, backup_retention, updated_by
)
VALUES (
  'BCTTG',
  'He thong so tay dien tu Binh chung Tang Thiet giap',
  'Asia/Bangkok',
  'vi',
  20,
  b'1',
  b'0',
  8,
  b'1',
  b'1',
  b'0',
  120,
  5,
  b'0',
  NULL,
  587,
  NULL,
  NULL,
  NULL,
  b'1',
  b'1',
  b'1',
  b'0',
  b'0',
  'WEEKLY',
  7,
  'system'
);

INSERT INTO home_modules (id, name, description, enabled, sort_order, updated_by)
VALUES
  ('banner', 'Banner Chinh', 'Hinh anh banner xoay vong tren dau trang chu', b'1', 1, 'system'),
  ('truyen-thong', 'Truyen thong', 'Cac noi dung truyen thong noi bat', b'1', 2, 'system'),
  ('net-tieu-bieu', 'Net tieu bieu', 'Noi dung net tieu bieu tren trang chu', b'1', 3, 'system'),
  ('thu-truong', 'Thu truong', 'Khoi noi dung ho so thu truong', b'1', 4, 'system'),
  ('anh-hung', 'Anh hung', 'Khoi noi dung ho so anh hung', b'1', 5, 'system'),
  ('ca-khuc', 'Ca khuc', 'Thu vien ca khuc hien thi ngoai trang chu', b'1', 6, 'system'),
  ('tin-tuc', 'Tin tuc', 'Module tin tuc mo rong cho homepage', b'0', 7, 'system');
