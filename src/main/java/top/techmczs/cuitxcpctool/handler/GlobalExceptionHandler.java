package top.techmczs.cuitxcpctool.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.exception.BaseException;
import top.techmczs.cuitxcpctool.result.Result;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BaseException.class)
    public Result<Object> baseExceptionHandler(BaseException ex) {
        log.error(ResponseMessageConstant.EVENT_ERROR, ex.getMessage());
        return Result.error(ResponseMessageConstant.ERROR);
    }

    // JWT 过期
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Object> jwtExpireHandler() {
        log.error(MessageConstant.TOKEN_TIME_OUT);
        return Result.error(ResponseMessageConstant.ERROR);
    }

    // JWT 签名错误
    @ExceptionHandler(SignatureException.class)
    public Result<Object> jwtSignatureHandler() {
        log.error(MessageConstant.ILLEGAL_TOKEN);
        return Result.error(ResponseMessageConstant.ERROR);
    }


    @ExceptionHandler(Exception.class)
    public Result<?> globalExceptionHandler(Exception e, HttpServletRequest request) throws Exception {
        // 如果是SSE请求，直接抛出，不返回JSON
        String contentType = request.getContentType();
        if (contentType != null && contentType.equals(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            throw e;
        }
        log.error(MessageConstant.SYSTEM_ERROR, e.getMessage());
        return Result.error(ResponseMessageConstant.ERROR);
    }
}
