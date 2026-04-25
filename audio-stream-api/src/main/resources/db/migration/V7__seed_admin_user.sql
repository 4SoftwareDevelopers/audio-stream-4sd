-- V7: Seed admin user
INSERT INTO admin_user (id, username, password, role, activo, created_at)
VALUES (
  'de9ef7c7-b130-427f-9f7d-ab94b2f4118b',
  'jordy_4sd',
  '$2y$12$RTRW1A.VlSKrHW/3nFobdeW0U.0XsP4b3rzTVvNRHuDo62Ikwfw6a',
  'admin',
  true,
  NOW()
);