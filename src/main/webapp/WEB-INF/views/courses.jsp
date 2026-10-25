<%@ include file="head.jspf" %>
<div class="page-heading"><h1>Course</h1><p>Pilih materi belajar. Tautan akan membuka situs penyedia materi pada tab baru.</p></div>
<c:choose><c:when test="${empty entries}"><div class="empty"><h2>Belum ada course</h2><p>Daftar ini akan terisi setelah pengelola menambahkan tautan materi.</p><a href="${pageContext.request.contextPath}/books">Buka katalog e-book</a></div></c:when>
<c:otherwise><div class="course-list"><c:forEach items="${entries}" var="entry"><article class="course"><div><h2><c:out value="${entry.title}"/></h2><p><c:out value="${entry.detail}"/></p></div><a class="button secondary" href="<c:out value='${entry.extra}'/>" target="_blank" rel="noopener noreferrer">Buka materi<span class="sr-only"> <c:out value="${entry.title}"/> (tab baru)</span></a></article></c:forEach></div></c:otherwise></c:choose>
<%@ include file="foot.jspf" %>
