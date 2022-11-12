package com.plumroc.springbootuploadminio.controller;


import com.plumroc.springbootuploadminio.pojo.Result;
import com.plumroc.springbootuploadminio.utils.MinIoUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * MinIoController
 *
 * @author PlumRoc
 * @date 2022-11-12
 */
@RestController
@RequestMapping("/minio")
public class MinIoController {

    @Resource
    private MinIoUtils minIoUtils;

    // 存储桶名称
    private static final String MINIO_BUCKET = "bucket";

    /**
     * 查询所有桶名称
     *
     * @return 桶
     */
    @RequestMapping("/queryBucketAll")
    public List<String> queryBucketAll() {
        return minIoUtils.listBucketNames();
    }

    /**
     * 列出存储桶中的所有对象名称
     *
     * @return 桶
     */
    @RequestMapping("/queryBucketObjectAll")
    public List<String> queryBucketObjectAll(String name) {
        return minIoUtils.listObjectNames(MINIO_BUCKET, name);
    }


    /**
     * 新增桶
     *
     * @param bucketName 桶名称
     * @return
     */
    @RequestMapping("/addBucketName")
    public String addBucketName(String bucketName) {
        boolean flag = minIoUtils.makeBucket(bucketName);
        if (!flag) {
            return "err";
        }
        return "ok";
    }

    /**
     * 文件上传
     *
     * @param files 文件名
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Result upload(@RequestParam(value = "files") MultipartFile files) {
        try {
            return Result.ok(minIoUtils.upload(files, MINIO_BUCKET, "public/"));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 文件下载
     *
     * @param minFileName 文件名称
     * @param response    请求对象
     */
    @GetMapping("/download")
    public void download(@RequestParam("minFileName") String minFileName, HttpServletResponse response) {
        minIoUtils.download(response, MINIO_BUCKET, minFileName);
    }


}
