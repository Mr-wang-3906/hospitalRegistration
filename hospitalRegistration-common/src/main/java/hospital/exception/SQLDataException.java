package hospital.exception;

/**
 * 业务异常
 */
public class SQLDataException extends RuntimeException {

    public SQLDataException() {
    }

    public SQLDataException(String msg) {
        super(msg);
    }

}
