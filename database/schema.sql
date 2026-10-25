CREATE TABLE accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('user','admin') NOT NULL DEFAULT 'user'
);
CREATE TABLE books (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  description TEXT NOT NULL,
  image VARCHAR(80) NOT NULL
);
CREATE TABLE courses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  topic VARCHAR(500) NOT NULL,
  url VARCHAR(1000) NOT NULL
);
CREATE TABLE feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  email VARCHAR(160) NOT NULL,
  comment TEXT NOT NULL
);

-- Data contoh baru, bukan ekspor database sumber.
INSERT INTO books(title,description,image) VALUES
('Matematika: catatan belajar','Contoh entri katalog untuk menyusun catatan aljabar, fungsi, dan latihan mandiri. Aplikasi ini menampilkan deskripsi, bukan menyediakan berkas buku.','Math.webp'),
('Mengenal sains','Contoh bacaan untuk mengelompokkan topik sains dan menulis ringkasan sebelum belajar lebih lanjut.','Science.webp'),
('Dasar fisika','Contoh entri katalog tentang gerak dan energi. Pengelola dapat mengganti judul serta ringkasannya.','Physics.webp'),
('Catatan sejarah','Contoh entri untuk menyimpan pengantar sejarah dan pertanyaan yang ingin dipelajari.','History1.jpeg');
INSERT INTO courses(title,topic,url) VALUES
('Belajar Java','Dokumentasi dan materi belajar Java dari dev.java.','https://dev.java/learn/'),
('Dasar HTML','Panduan HTML untuk memulai pengembangan halaman web.','https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Structuring_content');
