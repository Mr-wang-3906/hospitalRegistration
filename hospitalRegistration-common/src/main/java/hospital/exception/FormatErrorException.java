package hospital.exception;

/**
 * 账号不存在异常
 */
public class FormatErrorException extends BaseException {

    public FormatErrorException() {
    }

    public FormatErrorException(String msg) {
        super(msg);
    }

}
