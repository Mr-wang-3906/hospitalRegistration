package hospital.result;

import hospital.constant.MessageConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：200成功，500失败
    private String msg = "操作成功"; //若有错则改为错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = MessageConstant.Code_OK;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = MessageConstant.Code_OK;
        return result;
    }

    public static <T> Result<T> error(int code,String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> Result<T> userNotLogin(int code,String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = code;
        return result;
    }

}
