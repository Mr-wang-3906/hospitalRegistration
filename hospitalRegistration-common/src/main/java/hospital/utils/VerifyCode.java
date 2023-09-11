package hospital.utils;

import java.util.Random;

public class VerifyCode {

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

}
