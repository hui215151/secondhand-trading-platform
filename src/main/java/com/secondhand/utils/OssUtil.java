package com.secondhand.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.secondhand.config.OssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Component
public class OssUtil {
    @Autowired
    private OssConfig ossConfig;

    public String upload(MultipartFile file) {
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );

        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "goods/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());

            return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
}