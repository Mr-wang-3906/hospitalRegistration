package hospital.controller.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hospital.constant.MessageConstant;
import hospital.entity.Image;
import hospital.exception.AllException;
import hospital.mapper.ImageMapper;
import hospital.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;

import java.io.File;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@Slf4j
@Api(tags = "文件上传")
public class PngLoad {

    @Autowired
    private ImageMapper imageMapper;

    @ApiOperation(value = "文件上传")
    @PostMapping("/pngUpload")
    public Result<Map<String, Object>> savePic(@RequestBody String paramStr) {
        String base64Pic = "";
        String picName = "";
        JSONObject jsonObject = (JSONObject) JSON.parse(paramStr);
        base64Pic = jsonObject.getString("base64Pic");
        picName = jsonObject.getString("picName");

        if (base64Pic == null || base64Pic.isEmpty()) {
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.PIC_IS_EMPTY);
        }

        try {
            // 解码base64图片数据
            BASE64Decoder decoder = new BASE64Decoder();
            String baseValue = base64Pic.replaceAll(" ", "+");
            byte[] b = decoder.decodeBuffer(baseValue.replace("data:image/jpeg;base64,", ""));

            // 生成图片文件
            String imgFolderPath = "C:\\Users\\lenovo\\Desktop\\Java study\\学习笔记\\SpingBoot\\hospitalRegistration\\pngs";
            String imgFileName = UUID.randomUUID().toString() + ".jpg";
            String imgFilePath = imgFolderPath + File.separator + imgFileName;

            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }

            OutputStream out = Files.newOutputStream(Paths.get(imgFilePath));
            out.write(b);
            out.flush();
            out.close();

            // 将图片地址存储到数据库
            Image image = new Image();
            image.setPictureName(picName);
            image.setAddress(imgFilePath);
            imageMapper.save(image);

            return Result.success();
        } catch (Exception e) {
            return Result.error(MessageConstant.Code_Internal_Server_Error,MessageConstant.UNKNOWN_ERROR);
        }

    }
}
