package hospital.exception;

/**
 * 业务异常
 */
public class AllException extends RuntimeException {

    private final int code;

    public AllException(int code,String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
