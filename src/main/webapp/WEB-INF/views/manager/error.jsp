<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<section class="card card-body text-center py-5">
    <h1 class="h3">Không thể mở nội dung</h1>
    <p class="text-muted"><c:out value="${message}"/></p>
    <div><a class="btn btn-primary" href="${pageContext.request.contextPath}/manager">Về bảng điều khiển</a></div>
</section>
<%@ include file="fragments/footer.jsp" %>

