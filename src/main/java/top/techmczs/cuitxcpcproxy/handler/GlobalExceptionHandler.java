package top.techmczs.cuitxcpcproxy.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.techmczs.cuitxcpcproxy.exception.BaseException;
import top.techmczs.cuitxcpcproxy.result.Result;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BaseException.class)
    public Result<Object> baseExceptionHandler(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    // JWT 过期
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Object> jwtExpireHandler() {
        log.error("Token已过期");
        return Result.error("Token已过期，请重新审核");
    }

    // JWT 签名错误
    @ExceptionHandler(SignatureException.class)
    public Result<Object> jwtSignatureHandler(SignatureException ex) {
        log.error("Token签名非法");
        return Result.error("非法Token");
    }

//    // 兜底通用异常（防止服务崩溃）
//    @ExceptionHandler(Exception.class)
//    public Result<Object> globalExceptionHandler(Exception ex) {
//        log.error("系统未知异常",ex);
//        return Result.error("服务器异常，请联系管理员");
//    }

    @ExceptionHandler(Exception.class)
    public Result<?> globalExceptionHandler(Exception e, HttpServletRequest request) throws Exception {
        // 【核心修复】如果是SSE请求，直接抛出，不返回JSON
        String contentType = request.getContentType();
        if (contentType != null && contentType.equals(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            throw e;
        }

        log.error("系统异常：", e);
        return Result.error("系统未知异常：" + e.getMessage());
    }
}
