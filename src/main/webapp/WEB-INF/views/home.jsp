<%@ include file="head.jspf" %>
<section class="hero">
  <div class="hero-copy"><h1>Satu ruang untuk belajar lagi.</h1><p>Temukan bacaan di katalog, pilih topik course, dan simpan pengaturan akunmu di Hololearn.</p><a class="button" href="${pageContext.request.contextPath}/books">Jelajahi katalog e-book</a></div>
  <img src="${pageContext.request.contextPath}/assets/parabg.png" alt="" width="1968" height="1107" class="landscape">
</section>
<section class="pathways" aria-labelledby="pathways-title">
  <div><h2 id="pathways-title">Mulai dari yang ingin kamu pelajari.</h2><p>Katalog terbuka untuk semua pembaca. Akun member memberi akses ke daftar course dan halaman profil.</p></div>
  <div class="pathway"><h3>Baca pengantarnya</h3><p>Lihat sampul dan ringkasan untuk memilih topik berikutnya. Katalog ini belum menyediakan unduhan buku.</p><a href="${pageContext.request.contextPath}/books">Lihat e-book</a></div>
  <div class="pathway"><h3>Lanjutkan dengan course</h3><p>Daftar course menghubungkanmu ke materi belajar di situs penyedianya.</p><a href="${pageContext.request.contextPath}${empty user ? '/register' : '/courses'}">${empty user ? 'Buat akun member' : 'Pilih course'}</a></div>
</section>
<%@ include file="foot.jspf" %>
