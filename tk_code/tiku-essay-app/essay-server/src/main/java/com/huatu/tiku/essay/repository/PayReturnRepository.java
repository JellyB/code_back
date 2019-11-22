package com.huatu.tiku.essay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.huatu.tiku.essay.entity.PayReturn;

/**
 * Created by x6 on 2017/11/29.
 */
public interface PayReturnRepository extends JpaRepository<PayReturn, Long> {

	List<PayReturn> findByOutTradeNo(String outTradeNo);

}
