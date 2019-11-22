package com.huatu.tiku.course.netschool.api.v6;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-05-17 10:14 AM
 **/

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = InvoiceInterfaceV6.InvoiceInterfaceV6FallBack.class)
public interface InvoiceInterfaceV6 {


    /**
     * 开发票接口
     * @param params
     * @return
     */
    @PostMapping(value = "/v5/c/invoice/write_invoice", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    NetSchoolResponse writeInvoice(@RequestBody Map<String, Object> params);


    /**
     * 开票详情
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/invoice/invoice_detail")
    NetSchoolResponse InvoiceDetail(@RequestParam Map<String, Object> params);


    /**
     * 发送到邮箱
     * @param params
     * @return
     */
    @PostMapping(value = "/v5/c/invoice/send_invoice_to_email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    NetSchoolResponse SendInvoice2Email(@RequestParam Map<String, Object> params);


    @Slf4j
    @Component
    class InvoiceInterfaceV6FallBack implements Fallback<InvoiceInterfaceV6>{

        @Override
        public InvoiceInterfaceV6 create(Throwable throwable, HystrixCommand command) {
            return new InvoiceInterfaceV6() {
                @Override
                public NetSchoolResponse writeInvoice(Map<String, Object> params) {
                    log.error("invoice service v6 write fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse InvoiceDetail(Map<String, Object> params) {
                    log.error("invoice service v6 invoice detail fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse SendInvoice2Email(Map<String, Object> params) {
                    log.error("invoice service v6 send invoice 2 email fallback,params: {}, fall back reason: {} ",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }
}
