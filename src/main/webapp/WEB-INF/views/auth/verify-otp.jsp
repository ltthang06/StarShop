<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="verifyUrl" value="/verify-otp"/>
<c:url var="resendUrl" value="/register/resend"/>

<main class="container py-5">
    <div class="col-md-5 mx-auto">
        <h1 class="h3 mb-3">Xác thực email</h1>
        <p>Mã xác thực đã được gửi đến <strong><c:out value="${email}"/></strong>.</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}"/></div>
        </c:if>

        <form action="${verifyUrl}" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="email" value="<c:out value='${email}'/>">
            <div class="mb-3">
                <label class="form-label" for="code">Mã 6 chữ số</label>
                <input class="form-control" id="code" name="code"
                       inputmode="numeric" pattern="[0-9]{6}" required>
            </div>
            <button class="btn btn-danger w-100" type="submit">Xác thực</button>
        </form>

        <form action="${resendUrl}" method="post" class="mt-3">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="email" value="<c:out value='${email}'/>">
            <button class="btn btn-link p-0" type="submit">Gửi lại mã</button>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
