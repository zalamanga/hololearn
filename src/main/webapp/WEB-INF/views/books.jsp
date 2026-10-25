<%@ include file="head.jspf" %>
<div class="page-heading"><h1>Katalog e-book</h1><p>Sampul dan ringkasan untuk memilih bacaan. Entri awal adalah data contoh; berkas buku tidak disediakan.</p></div>
<c:choose><c:when test="${empty entries}"><div class="empty"><h2>Katalog belum terisi</h2><p>Pengelola belum menambahkan bacaan. Silakan kembali nanti.</p><a href="${pageContext.request.contextPath}/">Kembali ke beranda</a></div></c:when>
<c:otherwise><div class="book-grid"><c:forEach items="${entries}" var="entry"><article class="book"><img src="${pageContext.request.contextPath}/assets/<c:out value='${entry.extra}'/>" alt="Sampul <c:out value='${entry.title}'/>" width="240" height="320"><div><h2><c:out value="${entry.title}"/></h2><p><c:out value="${entry.detail}"/></p></div></article></c:forEach></div></c:otherwise></c:choose>
<%@ include file="foot.jspf" %>
