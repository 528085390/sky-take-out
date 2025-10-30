package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class UploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @RequestMapping("/upload")
    public Result<String> uploadFile(MultipartFile file){
        log.info("文件上传：{}", file);

        try {
            String originalFilename = file.getOriginalFilename();

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);

        }catch (Exception e){
            log.error("文件上传失败：{}", e.toString());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
