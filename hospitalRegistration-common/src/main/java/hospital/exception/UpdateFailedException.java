package hospital.exception;

/**
 * 登录失败
 */
public class UpdateFailedException extends BaseException{
    public UpdateFailedException(String msg){
        super(msg);
    }
}
