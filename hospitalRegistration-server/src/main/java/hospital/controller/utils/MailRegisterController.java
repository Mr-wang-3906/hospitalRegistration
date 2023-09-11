package hospital.controller.utils;

import hospital.constant.MessageConstant;
import hospital.controller.doctor.DoctorController;
import hospital.controller.patient.PatientController;
import hospital.exception.RegisterFailedException;
import hospital.result.Result;
import hospital.service.impl.PatientServiceImpl;
import hospital.utils.DataUtils;
import hospital.utils.VerifyCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 发送邮件验证注册
 **/
@RestController
@Slf4j
@Api(tags = "邮箱注册")
public class MailRegisterController {

    //这个是邮件类，须导入
    @Autowired
    JavaMailSenderImpl mailSender;

    //创建线程池对象
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);


    /**
     * 发送邮箱注册码邮箱
     */
    @GetMapping("/verify")
    @ResponseBody
    @ApiOperation(value = "发送邮箱注册码邮箱")
    public Result sendEmail(@RequestParam String emailAddress, HttpServletRequest request) {
        HttpSession session = request.getSession();
        //验证码
        String verCode = VerifyCode.setVerifyCode();
        //发送时间 --这里自己写了一个时间类，格式转换，用于邮件发送
        String time = DataUtils.format(new Date());
        Map<String, String> map = new HashMap<>();
        map.put("code", verCode);
        map.put("email", emailAddress);
        //验证码和邮箱，一起放入session中
        session.setAttribute("verCode", map);
        Map<String, String> codeMap = (Map<String, String>) session.getAttribute("verCode");
        //创建计时线程池，到时间自动移除验证码
        try {
            scheduledExecutorService.schedule(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    if (emailAddress.equals(codeMap.get("email"))) {
                        session.removeAttribute("verCode");
                    }
                }
            }, 5 * 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            //发送失败--服务器繁忙
            throw new RegisterFailedException(MessageConstant.SEND_EMAIL_FAILED);
        }
        //一下为发送邮件部分
        MimeMessage mimeMessage = null;
        MimeMessageHelper helper = null;
        try {
            //发送复杂的邮件
            mimeMessage = mailSender.createMimeMessage();
            //组装
            helper = new MimeMessageHelper(mimeMessage, true);
            //邮件标题
            helper.setSubject("注册账号验证码");
            //因为设置了邮件格式所以html标签有点多，后面的ture为支持识别html标签
            //想要不一样的邮件格式，百度搜索一个html编译器，自我定制。
            helper.setText("<h3>\n" +
                    "\t<span style=\"font-size:16px;\">亲爱的用户：</span> \n" +
                    "</h3>\n" +
                    "<p>\n" +
                    "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;您好！您正在进行邮箱验证，本次请求的验证码为：<span style=\"font-size:24px;color:#FFE500;\"> " + verCode + "</span>,本验证码5分钟内有效，请在5分钟内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。(这是一封自动发送的邮件，请不要直接回复）</span></span>\n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;background-color:#FFFFFF;\">xx 医院挂号系统</span></span></span> \n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;background-color:#FFFFFF;\">" + time + "</span><span style=\"font-size:18px;color:#000000;background-color:#FFFFFF;\"></span></span></span></span> \n" +
                    "</p>", true);
            //收件人
            helper.setTo(emailAddress);
            //发送方
            helper.setFrom("2892739899@qq.com");
            try {
                //发送邮件
                mailSender.send(mimeMessage);
            } catch (MailException e) {
                //邮箱是无效的，或者发送失败
                throw new RegisterFailedException(MessageConstant.VERIFY_ERROR);
            }
        } catch (MessagingException e) {
            //发送失败--服务器繁忙
            throw new RegisterFailedException(MessageConstant.SEND_EMAIL_FAILED);
        }
        //发送验证码成功
        DoctorController.verCode = verCode;
        return Result.success();
    }

}
