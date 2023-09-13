package hospital.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String VERIFY_ERROR = "邮箱格式不正确或邮箱未注册";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_ALREADY_EXISTS = "该用户名已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录或登录过期";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String SEND_EMAIL_FAILED = "发送失败,服务器繁忙";
    public static final String REGISTER_FAILED = "验证码错误";
    public static final String REGISTER_TIMEOUT = "验证码已过期";
    public static final String REGISTER_FAILED_BUSY = "注册失败,服务器繁忙";
    public static final String DELETE_FAILED_TEMPLATE = "排班模板至少要有一种挂号种类";
    public static final String DELETE_FAILED_DOCTOR = "已放号的排班不能修改";
    public static final String DELETE_FAILED_DOCTOR_SCHEDULE = "排班至少要有一种挂号种类";
    public static final String NET_ERROR = "网络错误";
    public static final String DATE_SET_ERROR = "设置排班不能早于今天";

}
