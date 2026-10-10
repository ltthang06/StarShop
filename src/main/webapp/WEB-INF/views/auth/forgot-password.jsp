<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="forgotUrl" value="/forgot-password"/>

<main class="container py-5">
    <div class="col-md-5 mx-auto">
        <h1 class="h3 mb-4">Quên mật khẩu</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${forgotUrl}" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="mb-3">
                <label class="form-label" for="email">Email tài khoản</label>
                <input class="form-control" id="email" name="email"
                       value="<c:out value='${email}'/>"
                       type="email" required>
            </div>
            <button class="btn btn-danger w-100" type="submit">Gửi mã</button>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
