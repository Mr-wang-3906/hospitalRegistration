package hospital.exception;

/**
 * 账号不存在异常
 */
public class NetException extends BaseException {

    public NetException() {
    }

    public NetException(String msg) {
        super(msg);
    }

}
