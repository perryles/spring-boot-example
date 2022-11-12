package com.plumroc.springbootuploadminio.utils;


import com.google.common.collect.Lists;
import com.plumroc.springbootuploadminio.dto.MinIoUploadResDTO;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MinIo工具类
 *
 * @author PlumRoc
 * @date 2022-11-12
 */
@Component
public class MinIoUtils {

    @Resource
    private MinioClient minioClient;

    private static final String SEPARATOR_DOT = ".";

    private static final String SEPARATOR_ACROSS = "-";

    private static final String SEPARATOR_STR = "";

    // 存储桶名称
    private static final String chunkBucKet = "bucket";

    /**
     * 不排序
     */
    public final static boolean NOT_SORT = false;

    /**
     * 排序
     */
    public final static boolean SORT = true;

    /**
     * 默认过期时间(分钟)
     */
    private final static Integer DEFAULT_EXPIRY = 60;

    /**
     * 删除分片
     */
    public final static boolean DELETE_CHUNK_OBJECT = true;
    /**
     * 不删除分片
     */
    public final static boolean NOT_DELETE_CHUNK_OBJECT = false;

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 桶名
     * @return boolean
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }


    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean isExist = bucketExists(bucketName);
        if (isExist) {
            return true;
        }
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        return true;
    }

    /**
     * 列出所有存储桶
     *
     * @return 存储桶
     */
    @SneakyThrows
    private List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 列出所有存储桶名称
     *
     * @return 存储桶名称
     */
    public List<String> listBucketNames() {
        List<Bucket> list = listBuckets();
        return list.stream().filter(Objects::nonNull).map(Bucket::name).collect(Collectors.toList());
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名称
     * @return boolean
     */
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            // 有对象文件，则删除失败
            if (item.size() > 0) {
                return false;
            }
        }
        // 删除存储桶，注意，只有存储桶为空时才能删除成功。
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        return !bucketExists(bucketName);
    }

    /**
     * 列出存储桶中的所有对象
     *
     * @param bucketName 存储桶名称
     * @return 存储桶对象
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 列出存储桶中的所有对象名称
     *
     * @param bucketName 存储桶名称
     * @return 存储桶对象名称
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> ret = Lists.newArrayList();
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            ret.add(item.objectName());
        }
        return ret;
    }


    /**
     * 获取对象文件名称列表
     *
     * @param bucketName 存储桶名称
     * @param prefix     对象名称前缀(文件夹 /xx/xx/xxx.jpg 中的 /xx/xx/)
     * @return objectNames
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName, String prefix) {
        return listObjectNames(bucketName, prefix, NOT_SORT);
    }


    /**
     * 获取对象文件名称列表
     *
     * @param bucketName 存储桶名称
     * @param prefix     对象名称前缀(文件夹 /xx/xx/xxx.jpg 中的 /xx/xx/)
     * @param sort       是否排序(升序)
     * @return objectNames
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName, String prefix, Boolean sort) {
        boolean flag = bucketExists(bucketName);
        if (flag) {

            ListObjectsArgs listObjectsArgs;
            if (null == prefix) {
                listObjectsArgs = ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build();
            } else {
                listObjectsArgs = ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(true)
                        .build();
            }
            Iterable<Result<Item>> chunks = minioClient.listObjects(listObjectsArgs);
            List<String> chunkPaths = new ArrayList<>();
            for (Result<Item> item : chunks) {
                chunkPaths.add(item.get().objectName());
            }
            if (sort) {
                chunkPaths.sort(new Str2IntComparator(false));
            }
            return chunkPaths;
        }
        return new ArrayList<>();
    }

    /**
     * 在桶下创建文件夹,文件夹层级结构根据参数决定
     *
     * @param bucket 桶名称
     * @param WotDir 格式为 xxx/xxx/xxx/
     */
    @SneakyThrows
    public String createDirectory(String bucket, String WotDir) {
        if (!this.bucketExists(bucket)) {
            return null;
        }
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(WotDir).stream(
                        new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
        return WotDir;
    }


    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称
     */
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        if (!bucketExists(bucketName)) {
            return false;
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return true;
    }

    /**
     * 删除指定桶的多个文件对象
     *
     * @param bucketName  桶
     * @param objectNames 文件列表
     * @return 返回删除错误的对象列表，全部删除成功，返回空列表
     */
    @SneakyThrows
    public List<String> removeObjects(String bucketName, List<String> objectNames) {
        if (!bucketExists(bucketName)) {
            return new ArrayList<>();
        }
        List<DeleteObject> deleteObjects = new ArrayList<>(objectNames.size());
        for (String objectName : objectNames) {
            deleteObjects.add(new DeleteObject(objectName));
        }
        List<String> deleteErrorNames = new ArrayList<>();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(deleteObjects)
                        .build());
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            deleteErrorNames.add(error.objectName());
        }
        return deleteErrorNames;
    }


    /**
     * 获取访问对象的外链地址
     * 获取文件的下载url
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间(分钟) 最大为7天 超过7天则默认最大值
     * @return viewUrl
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName, Integer expiry) {
        expiry = expiryHandle(expiry);
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expiry)
                        .build()
        );
    }


    /**
     * 创建上传文件对象的外链
     *
     * @param bucketName 存储桶名称
     * @param objectName 欲上传文件对象的名称
     * @return uploadUrl
     */
    public String createUploadUrl(String bucketName, String objectName) {
        return createUploadUrl(bucketName, objectName, DEFAULT_EXPIRY);
    }

    /**
     * 创建上传文件对象的外链
     *
     * @param bucketName 存储桶名称
     * @param objectName 欲上传文件对象的名称
     * @param expiry     过期时间(分钟) 最大为7天 超过7天则默认最大值
     * @return uploadUrl
     */
    @SneakyThrows
    public String createUploadUrl(String bucketName, String objectName, Integer expiry) {
        expiry = expiryHandle(expiry);
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expiry)
                        .build()
        );
    }

    /**
     * 批量下载
     *
     * @param directory
     * @return
     */
    @SneakyThrows
    public List<String> downLoadMore(String bucket, String directory) {
        Iterable<Result<Item>> objs = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).prefix(directory).useUrlEncodingType(false).build());
        List<String> list = new ArrayList<>();
        for (Result<Item> result : objs) {
            String objectName = null;
            objectName = result.get().objectName();
            StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            if (statObject != null && statObject.size() > 0) {
                String fileurl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket).object(statObject.object()).method(Method.GET).build());
                list.add(fileurl);
            }
        }
        return list;
    }


    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @param bucketName    存储桶名称
     * @param directory     储存位置
     * @return 成功对象
     * @throws Exception
     */
    public MinIoUploadResDTO upload(MultipartFile multipartFile, String bucketName, String directory) throws Exception {
        if (!this.bucketExists(bucketName)) {
            boolean flag = this.makeBucket(bucketName);
            if (!flag) {
                return new MinIoUploadResDTO(null, null);
            }
        }
        InputStream inputStream = multipartFile.getInputStream();
        directory = Optional.ofNullable(directory).orElse("");
        String minFileName = directory + minFileName(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        System.out.println(minFileName);
        //上传文件到指定目录
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(minFileName)
                .contentType(multipartFile.getContentType())
                .stream(inputStream, inputStream.available(), -1)
                .build());
        inputStream.close();
        // 返回生成文件名、访问路径
        return new MinIoUploadResDTO(minFileName, getObjectUrl(bucketName, minFileName, DEFAULT_EXPIRY));
    }

    /**
     * 下载文件
     *
     * @param response    HttpServletResponse对象
     * @param bucketName  存储桶名称
     * @param minFileName 文件名称
     */
    @SneakyThrows
    public void download(HttpServletResponse response, String bucketName, String minFileName) {
        InputStream fileInputStream = null;
        fileInputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(minFileName).build());
        response.setHeader("Content-Disposition", "attachment;filename=" + minFileName);
        response.setContentType("application/force-download");
        response.setCharacterEncoding("utf-8");
        IOUtils.copy(fileInputStream, response.getOutputStream());
        fileInputStream.close();

    }


    /**
     * 批量创建分片上传外链
     *
     * @param bucketName 存储桶名称
     * @param objectMD5  欲上传分片文件主文件的MD5
     * @param chunkCount 分片数量
     * @return uploadChunkUrls
     */
    public List<String> createUploadChunkUrlList(String bucketName, String objectMD5, Integer chunkCount) {
        if (null == bucketName) {
            bucketName = chunkBucKet;
        }
        if (null == objectMD5) {
            return null;
        }
        objectMD5 += "/";
        if (null == chunkCount || 0 == chunkCount) {
            return null;
        }
        List<String> urlList = new ArrayList<>(chunkCount);
        for (int i = 1; i <= chunkCount; i++) {
            String objectName = objectMD5 + i + ".chunk";
            urlList.add(createUploadUrl(bucketName, objectName, DEFAULT_EXPIRY));
        }
        return urlList;
    }

    /**
     * 创建指定序号的分片文件上传外链
     *
     * @param bucketName 存储桶名称
     * @param objectMD5  欲上传分片文件主文件的MD5
     * @param partNumber 分片序号
     * @return uploadChunkUrl
     */
    public String createUploadChunkUrl(String bucketName, String objectMD5, Integer partNumber) {
        if (null == bucketName) {
            bucketName = chunkBucKet;
        }
        if (null == objectMD5) {
            return null;
        }
        objectMD5 += "/" + partNumber + ".chunk";
        return createUploadUrl(bucketName, objectMD5, DEFAULT_EXPIRY);
    }


    /**
     * 获取分片文件名称列表
     *
     * @param bucketName 存储桶名称
     * @param ObjectMd5  对象Md5
     * @return objectChunkNames
     */
    public List<String> listChunkObjectNames(String bucketName, String ObjectMd5) {
        if (null == bucketName) {
            bucketName = chunkBucKet;
        }
        if (null == ObjectMd5) {
            return null;
        }
        return listObjectNames(bucketName, ObjectMd5, SORT);
    }

    /**
     * 获取分片名称地址HashMap key=分片序号 value=分片文件地址
     *
     * @param bucketName 存储桶名称
     * @param ObjectMd5  对象Md5
     * @return objectChunkNameMap
     */
    public Map<Integer, String> mapChunkObjectNames(String bucketName, String ObjectMd5) {
        if (null == bucketName) {
            bucketName = chunkBucKet;
        }
        if (null == ObjectMd5) {
            return null;
        }
        List<String> chunkPaths = listObjectNames(bucketName, ObjectMd5);
        if (null == chunkPaths || chunkPaths.size() == 0) {
            return null;
        }
        Map<Integer, String> chunkMap = new HashMap<>(chunkPaths.size());
        for (String chunkName : chunkPaths) {
            Integer partNumber = Integer.parseInt(chunkName.substring(chunkName.indexOf("/") + 1, chunkName.lastIndexOf(".")));
            chunkMap.put(partNumber, chunkName);
        }
        return chunkMap;
    }


    /**
     * 合并分片文件成对象文件
     *
     * @param chunkBucKetName   分片文件所在存储桶名称
     * @param composeBucketName 合并后的对象文件存储的存储桶名称
     * @param chunkNames        分片文件名称集合
     * @param objectName        合并后的对象文件名称
     * @return true/false
     */
    @SneakyThrows
    public boolean composeObject(String chunkBucKetName, String composeBucketName, List<String> chunkNames, String objectName, boolean isDeleteChunkObject) {
        if (null == chunkBucKetName) {
            chunkBucKetName = chunkBucKet;
        }
        List<ComposeSource> sourceObjectList = new ArrayList<>(chunkNames.size());
        for (String chunk : chunkNames) {
            sourceObjectList.add(
                    ComposeSource.builder()
                            .bucket(chunkBucKetName)
                            .object(chunk)
                            .build()
            );
        }
        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(composeBucketName)
                        .object(objectName)
                        .sources(sourceObjectList)
                        .build()
        );
        if (isDeleteChunkObject) {
            removeObjects(chunkBucKetName, chunkNames);
        }
        return true;
    }

    /**
     * 合并分片文件成对象文件
     *
     * @param bucketName 存储桶名称
     * @param chunkNames 分片文件名称集合
     * @param objectName 合并后的对象文件名称
     * @return true/false
     */
    public boolean composeObject(String bucketName, List<String> chunkNames, String objectName) {
        return composeObject(chunkBucKet, bucketName, chunkNames, objectName, NOT_DELETE_CHUNK_OBJECT);
    }

    /**
     * 合并分片文件成对象文件
     *
     * @param bucketName 存储桶名称
     * @param chunkNames 分片文件名称集合
     * @param objectName 合并后的对象文件名称
     * @return true/false
     */
    public boolean composeObject(String bucketName, List<String> chunkNames, String objectName, boolean isDeleteChunkObject) {
        return composeObject(chunkBucKet, bucketName, chunkNames, objectName, isDeleteChunkObject);
    }

    /**
     * 合并分片文件，合并成功后删除分片文件
     *
     * @param bucketName 存储桶名称
     * @param chunkNames 分片文件名称集合
     * @param objectName 合并后的对象文件名称
     * @return true/false
     */
    public boolean composeObjectAndRemoveChunk(String bucketName, List<String> chunkNames, String objectName) {
        return composeObject(chunkBucKet, bucketName, chunkNames, objectName, DELETE_CHUNK_OBJECT);
    }


    /**
     * @param originalFileName
     * @return java.lang.String
     * @Description 生成上传文件名
     * @author exe.wangtaotao
     * @date 2020/10/21 15:07
     */
    private String minFileName(String originalFileName) {
        String suffix = originalFileName;
        if (originalFileName.contains(SEPARATOR_DOT)) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf(SEPARATOR_DOT));
        }
        return UUID.randomUUID().toString().replace(SEPARATOR_ACROSS, SEPARATOR_STR).toUpperCase() + suffix;
    }


    /**
     * 将分钟数转换为秒数
     *
     * @param expiry 过期时间(分钟数)
     * @return expiry
     */
    private static int expiryHandle(Integer expiry) {
        expiry = expiry * 60;
        if (expiry > 604800) {
            return 604800;
        }
        return expiry;
    }

    static class Str2IntComparator implements Comparator<String> {
        private final boolean reverseOrder; // 是否倒序

        public Str2IntComparator(boolean reverseOrder) {
            this.reverseOrder = reverseOrder;
        }

        @Override
        public int compare(String arg0, String arg1) {
            Integer intArg0 = Integer.parseInt(arg0.substring(arg0.indexOf("/") + 1, arg0.lastIndexOf(".")));
            Integer intArg1 = Integer.parseInt(arg1.substring(arg1.indexOf("/") + 1, arg1.lastIndexOf(".")));
            if (reverseOrder) {
                return intArg1 - intArg0;
            } else {
                return intArg0 - intArg1;
            }
        }
    }

}
