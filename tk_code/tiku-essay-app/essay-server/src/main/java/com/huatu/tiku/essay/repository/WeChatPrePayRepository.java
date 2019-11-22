package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.WeChatPrePay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeChatPrePayRepository extends JpaRepository<WeChatPrePay,Long> {

    WeChatPrePay findByAttach(String attach);
}
