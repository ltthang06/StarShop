<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="resetUrl" value="/reset-password"/>

<main class="container py-5">
    <div class="col-md-5 mx-auto">
        <h1 class="h3 mb-4">Đặt lại mật khẩu</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>

        <form action="${resetUrl}" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" id="email" name="email"
                       value="<c:out value='${email}'/>"
                       type="email" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="code">Mã xác thực</label>
                <input class="form-control" id="code" name="code"
                       inputmode="numeric" pattern="[0-9]{6}" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Mật khẩu mới</label>
                <input class="form-control" id="password" name="password"
                       type="password" minlength="8" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmPassword">Nhập lại mật khẩu</label>
                <input class="form-control" id="confirmPassword"
                       name="confirmPassword" type="password" minlength="8" required>
            </div>
            <button class="btn btn-danger w-100" type="submit">Đổi mật khẩu</button>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
