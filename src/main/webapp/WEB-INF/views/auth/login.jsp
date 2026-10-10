<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="loginUrl" value="/login"/>
<c:url var="registerUrl" value="/register"/>
<c:url var="forgotUrl" value="/forgot-password"/>

<main class="container py-5">
    <div class="col-md-5 mx-auto">
        <h1 class="h3 mb-4">Đăng nhập</h1>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger">Email hoặc mật khẩu không đúng, hoặc tài khoản chưa hoạt động.</div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>

        <form action="${loginUrl}" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" id="email" name="email"
                       type="email" required autocomplete="username">
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Mật khẩu</label>
                <input class="form-control" id="password" name="password"
                       type="password" required autocomplete="current-password">
            </div>
            <button class="btn btn-danger w-100" type="submit">Đăng nhập</button>
        </form>

        <div class="d-flex justify-content-between mt-3">
            <a href="${registerUrl}">Tạo tài khoản</a>
            <a href="${forgotUrl}">Quên mật khẩu?</a>
        </div>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
