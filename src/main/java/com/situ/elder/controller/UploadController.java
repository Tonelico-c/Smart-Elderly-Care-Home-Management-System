package com.situ.elder.controller;

import com.situ.elder.utils.AliOSSUtil;
import com.situ.elder.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class UploadController {

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        // 生成随机 UUID
        String uuid = UUID.randomUUID().toString().replace("-","");
        // 原始文件名
        String fileName =file.getOriginalFilename();
        // 后缀名
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 新文件名
        String newFileName = uuid + suffix;

        String url = "";
        try {
            url = AliOSSUtil.uploadFile(newFileName, file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.ok("上传成功",url);
    }
}
