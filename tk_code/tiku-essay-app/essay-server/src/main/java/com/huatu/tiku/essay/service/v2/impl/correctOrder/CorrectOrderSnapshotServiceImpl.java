package com.huatu.tiku.essay.service.v2.impl.correctOrder;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderSnapshotChannelEnum;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayTeacherRepository;
import com.huatu.tiku.essay.repository.snapshot.CorrectOrderSnapshotRepository;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSnapshotVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 老师批改订单行为日志查询
 */
@Service
public class CorrectOrderSnapshotServiceImpl implements CorrectOrderSnapshotService {

    @Autowired
    CorrectOrderSnapshotRepository correctOrderSnapshotRepository;

    @Autowired
    EssayTeacherRepository essayTeacherRepository;

    /**
     * 查询订单行为记录
     *
     * @param orderId
     * @return
     */
    public PageUtil<CorrectOrderSnapshotVo> getOrderSnapshot(long orderId, int page, int size) {

        PageRequest pageable = new PageRequest(page - 1, size, Sort.Direction.DESC, "id");
        Specification<CorrectOrderSnapshot> specification = new Specification<CorrectOrderSnapshot>() {
            @Override
            public Predicate toPredicate(Root<CorrectOrderSnapshot> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = Lists.newArrayList();
                list.add(criteriaBuilder.equal(root.get("orderId").as(Long.class), orderId));
                Predicate[] p = new Predicate[list.size()];
                return criteriaBuilder.and(list.toArray(p));
            }
        };
        Page<CorrectOrderSnapshot> paperList = correctOrderSnapshotRepository.findAll(specification, pageable);
        List<CorrectOrderSnapshot> list = paperList.getContent();

        //组装数据
        List<CorrectOrderSnapshotVo> snapshotVoList = new ArrayList<>();
        list.stream().forEach(snapshot -> {
            CorrectOrderSnapshotVo snapshotVo = new CorrectOrderSnapshotVo();
            BeanUtils.copyProperties(snapshot, snapshotVo);
            snapshotVo.setCreatorId(snapshot.getCreator());
            snapshotVo.setCreatorName(snapshot.getCreator());
            snapshotVo.setPhoneNum("");

            //批改老师手机号
            if (null != snapshot.getCorrectTeacherId()) {
                EssayTeacher essayTeacher = essayTeacherRepository.findByIdAndStatus(snapshot.getCorrectTeacherId(), EssayStatusEnum.NORMAL.getCode());
                if (null != essayTeacher) {
                    snapshotVo.setPhoneNum(essayTeacher.getPhoneNum());
                }
            }
            snapshotVoList.add(snapshotVo);
        });

        snapshotVoList.stream().sorted();
        long totalElements = paperList.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        PageUtil pageResult = PageUtil.builder()
                .result(snapshotVoList)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return pageResult;
    }

    @Override
    public void save(CorrectOrderSnapshot correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum operateEnum) {
        correctOrderSnapshot.setOperate(operateEnum.getCode());
        correctOrderSnapshot.setOperateName(operateEnum.getName());
        correctOrderSnapshot.setGmtCreate(new Date());
        correctOrderSnapshot.setStatus(EssayStatusEnum.NORMAL.getCode());
        correctOrderSnapshotRepository.save(correctOrderSnapshot);
    }

    @Override
    public boolean checkNoAdmin(CorrectOrder correctOrder) {
        List<CorrectOrderSnapshot> snapshotList = correctOrderSnapshotRepository.findByOrderIdAndStatus(correctOrder.getId(), EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(snapshotList)) {
            return true;
        }
        java.util.function.Predicate<CorrectOrderSnapshot> isAdmin = (snapshot -> {
            ArrayList<Integer> operate = Lists.newArrayList(CorrectOrderStatusEnum.OperateEnum.DISPATCH_MANUAL.getCode(),
                    CorrectOrderStatusEnum.OperateEnum.RECALL.getCode(),
                    CorrectOrderStatusEnum.OperateEnum.INIT.getCode());     //管理员干涉订单的操作
            return operate.contains(snapshot.getOperate());
        });
        List<CorrectOrderSnapshot> collect = snapshotList.stream().filter(i -> i.getChannel() == CorrectOrderSnapshotChannelEnum.ADMIN.getValue())
                .filter(isAdmin::test).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect);
    }

    @Override
    public List<CorrectOrderSnapshot> findByOrderIdAndOperate(long orderId, CorrectOrderStatusEnum.OperateEnum operate) {
        return correctOrderSnapshotRepository.findByOrderIdAndOperateAndStatus(orderId,operate.getCode(),EssayStatusEnum.NORMAL.getCode());
    }


}
