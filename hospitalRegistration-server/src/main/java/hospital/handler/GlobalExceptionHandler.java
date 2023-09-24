package hospital.handler;

import hospital.constant.MessageConstant;
import hospital.exception.AllException;
import hospital.exception.UserNotLoginException;
import hospital.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(AllException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getCode(),ex.getMessage());
    }

    /**
     * 捕获未登录状态
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(UserNotLoginException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.userNotLogin(MessageConstant.Code_Unauthorized,ex.getMessage());
    }

    /**
     * 处理sql异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // Duplicate entry 'lisi' for key 'employee.idx_username'
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(MessageConstant.Code_Internal_Server_Error,msg);
        }else {
            return Result.error(MessageConstant.Code_Internal_Server_Error,MessageConstant.UNKNOWN_ERROR);
        }
    }

}
