package hospital.exception;

/**
 * 考试不存在异常
 */
public class ExamNotFoundException extends BaseException {

    public ExamNotFoundException() {
    }

    public ExamNotFoundException(String msg) {
        super(msg);
    }

}
