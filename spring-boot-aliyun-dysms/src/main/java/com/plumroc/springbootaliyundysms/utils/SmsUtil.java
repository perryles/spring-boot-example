package com.plumroc.springbootaliyundysms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.plumroc.springbootaliyundysms.config.DySmsConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SmsUtil {

    @Resource
    private DySmsConfig dySmsConfig;

    public static CommonResponse sendSms(String accessKeyId, String accessSecret, String phoneNumbers, String signName, String templateCode, String params) throws ClientException {
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //设置区域与秘钥
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
        //创建客户端
        IAcsClient client = new DefaultAcsClient(profile);
        //创建请求体
        CommonRequest request = new CommonRequest();
        //设置请求方式为post请求
        request.setSysMethod(MethodType.POST);
        //设置域名(固定域名)
        request.setSysDomain("dysmsapi.aliyuncs.com");
        //设置短信版本
        request.setSysVersion("2017-05-25");
        //设置执行的方法
        request.setSysAction("SendSms");
        //设置区域id
        request.putQueryParameter("RegionId", "cn-hangzhou");
        //设置电话号码,多个电话号码逗号隔开
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        //设置短信签名
        request.putQueryParameter("SignName", signName);
        //设置选择的模板
        request.putQueryParameter("TemplateCode", templateCode);
        //设置参数
        request.putQueryParameter("TemplateParam", params);
        //发送短信请求
        CommonResponse response = client.getCommonResponse(request);
        System.out.println(response.getData());
        //返回响应结果
        return response;
    }

    /**
     * 短信发送
     *
     * @param phoneNumbers 手机号
     * @param params       参数
     * @return
     * @throws ClientException
     */
    public SendSmsResponse sendSms(String phoneNumbers, String params) throws ClientException {
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", dySmsConfig.getAccessKeyId(), dySmsConfig.getAccessSecret());
        DefaultProfile.addEndpoint("cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phoneNumbers);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(dySmsConfig.getSignName());
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(dySmsConfig.getTemplateCode());
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        request.setTemplateParam(params);

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }

    /**
     * 查询短信信息
     *
     * @param bizId        流水号
     * @param phoneNumbers 手机号
     * @return
     * @throws ClientException
     */
    public QuerySendDetailsResponse querySendDetails(String bizId, String phoneNumbers) throws ClientException {

        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", dySmsConfig.getAccessKeyId(), dySmsConfig.getAccessSecret());
        DefaultProfile.addEndpoint("cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象
        QuerySendDetailsRequest request = new QuerySendDetailsRequest();
        //必填-号码
        request.setPhoneNumber(phoneNumbers);
        //可选-流水号
        request.setBizId(bizId);
        //必填-发送日期 支持30天内记录查询，格式yyyyMMdd
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
        request.setSendDate(ft.format(new Date()));
        //必填-页大小
        request.setPageSize(10L);
        //必填-当前页码从1开始计数
        request.setCurrentPage(1L);

        //hint 此处可能会抛出异常，注意catch
        QuerySendDetailsResponse querySendDetailsResponse = acsClient.getAcsResponse(request);

        return querySendDetailsResponse;
    }


}
