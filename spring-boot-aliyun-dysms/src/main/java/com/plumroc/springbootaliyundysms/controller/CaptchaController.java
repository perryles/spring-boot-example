
package com.plumroc.springbootaliyundysms.controller;

import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.plumroc.springbootaliyundysms.utils.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 验证码操作处理
 *
 * @author plumroc
 */
@Slf4j
@RestController
public class CaptchaController {

    @Resource
    private SmsUtil smsUtil;

    @GetMapping("sms/{phone}")
    public QuerySendDetailsResponse index(@PathVariable String phone) throws ClientException, InterruptedException {
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        log.info("随机验证码=" + code);
        SendSmsResponse response = smsUtil.sendSms(phone, "{\"code\":\"" + code + "\"}");
        log.info("短信接口返回的数据----------------");
        log.info("Code=" + response.getCode());
        log.info("Message=" + response.getMessage());
        log.info("RequestId=" + response.getRequestId());
        log.info("BizId=" + response.getBizId());

        Thread.sleep(3000L);

        QuerySendDetailsResponse querySendDetailsResponse = null;
        //查明细
        if (response.getCode() != null && response.getCode().equals("OK")) {
            querySendDetailsResponse = smsUtil.querySendDetails(response.getBizId(), phone);
            log.info("短信明细查询接口返回数据----------------");
            log.info("Code=" + querySendDetailsResponse.getCode());
            log.info("Message=" + querySendDetailsResponse.getMessage());
            int i = 0;
            for (QuerySendDetailsResponse.SmsSendDetailDTO smsSendDetailDTO : querySendDetailsResponse.getSmsSendDetailDTOs()) {
                log.info("SmsSendDetailDTO[" + i + "]:");
                log.info("Content=" + smsSendDetailDTO.getContent());
                log.info("ErrCode=" + smsSendDetailDTO.getErrCode());
                log.info("OutId=" + smsSendDetailDTO.getOutId());
                log.info("PhoneNum=" + smsSendDetailDTO.getPhoneNum());
                log.info("ReceiveDate=" + smsSendDetailDTO.getReceiveDate());
                log.info("SendDate=" + smsSendDetailDTO.getSendDate());
                log.info("SendStatus=" + smsSendDetailDTO.getSendStatus());
                log.info("Template=" + smsSendDetailDTO.getTemplateCode());
            }
            log.info("TotalCount=" + querySendDetailsResponse.getTotalCount());
            log.info("RequestId=" + querySendDetailsResponse.getRequestId());
        }
        return querySendDetailsResponse;
    }
}
