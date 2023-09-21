package hospital.utils;

import java.util.Random;

public class Code {

    //生成六位邮箱验证码
    public static String setVerifyCode() {
        StringBuilder str = new StringBuilder();
        char[] ch = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char num = ch[random.nextInt(ch.length)];
            str.append(num);
        }
        return str.toString();
    }

    //生成用户下单单号
    public static String setOddNumber() {
        StringBuilder str = new StringBuilder();
        char[] en = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            char num = en[random.nextInt(en.length)];
            str.append(num);
        }
        char[] ch = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (int i = 0; i < 10; i++) {
            char num = ch[random.nextInt(ch.length)];
            str.append(num);
        }

        return str.toString();
    }
}
