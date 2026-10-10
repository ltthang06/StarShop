<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="registerUrl" value="/register"/>

<main class="container py-5">
    <div class="col-md-6 mx-auto">
        <h1 class="h3 mb-4">Đăng ký tài khoản</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${registerUrl}" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="mb-3">
                <label class="form-label" for="fullName">Họ tên</label>
                <input class="form-control" id="fullName" name="fullName"
                       value="<c:out value='${fullName}'/>"
                       maxlength="100" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" id="email" name="email"
                       value="<c:out value='${email}'/>"
                       type="email" maxlength="150" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Mật khẩu</label>
                <input class="form-control" id="password" name="password"
                       type="password" minlength="8" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmPassword">Nhập lại mật khẩu</label>
                <input class="form-control" id="confirmPassword"
                       name="confirmPassword" type="password" minlength="8" required>
            </div>
            <button class="btn btn-danger w-100" type="submit">Đăng ký</button>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
