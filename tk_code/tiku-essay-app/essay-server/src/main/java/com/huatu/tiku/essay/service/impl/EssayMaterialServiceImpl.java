package com.huatu.tiku.essay.service.impl;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.collect.Lists;
import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayMaterialConstant;
import com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionMaterialConstant;
import com.huatu.tiku.essay.constant.cache.CorrectRedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.huatu.tiku.essay.repository.EssayMaterialRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionMaterialRepository;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.util.file.HtmlFileUtil;
import com.itextpdf.text.BadElementException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
@SuppressWarnings("ALL")
//@Transactional
@Service
@Slf4j
public class EssayMaterialServiceImpl implements EssayMaterialService {
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayQuestionMaterialRepository essayQuestionMaterialRepository;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public List<EssayMaterialVO> findMaterialsByPaperId(long paperId) {
        List<EssayMaterialVO> list = Lists.newLinkedList();
        List<EssayMaterial> questionMaterials = essayMaterialRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                (paperId, EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus(),EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
        for (EssayMaterial material : questionMaterials) {
            long materialId = material.getId();
            EssayMaterialVO vo = EssayMaterialVO.builder()
                    .content(material.getContent())
                    //材料内容
                    .id(materialId)
                    //材料id
                    .sort(material.getSort())
                    //材料序号
                    .build();

            list.add(vo);
        }
        return list;
    }

    @Override
    public EssayMaterialVO insertMaterial(EssayMaterialVO essayMaterial, String userId) throws InvocationTargetException, IllegalAccessException, BadElementException, BizException, IOException {
        Long paperId = essayMaterial.getPaperId();

        String content= essayMaterial.getContent();
        log.info("preImg={}",content);
        content = htmlFileUtil.imgManage(content,userId,0);
        log.info("afterImg={}",content);
        content = htmlFileUtil.htmlManage(content);
        log.info("afterHtml={}",content);
        EssayMaterial material = EssayMaterial.builder().content(content)
                .paperId(essayMaterial.getPaperId())
                .sort(essayMaterial.getSort())
                .build();
        material.setGmtCreate(new Date());
        material.setCreator(userId+"");
        material.setStatus(EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus());
        material.setBizStatus(EssayQuestionMaterialConstant.EssayQuestionMaterialBizStatusEnum.INIT.getBizStatus());
        EssayMaterial target =  essayMaterialRepository.save(material);
        //编辑材料。试卷状态置为（下线，未审核）
        essayPaperBaseRepository.modifyPaperToOffline(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(),EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(),paperId);
        EssayMaterialVO result = EssayMaterialVO.builder().id(target.getId())
                .content(target.getContent())
                .paperId(target.getPaperId())
                .sort(target.getSort())
                .build();
        return result;

    }


    @Override
    public List<EssayMaterial> saveMaterial(AdminMaterialListVO adminMaterialListVO, String userId) {
        List<EssayMaterial> essayMaterials = new ArrayList<>();
        if(null == adminMaterialListVO&&CollectionUtils.isEmpty(adminMaterialListVO.getMaterialList())){
            log.info("参数异常,材料列表不能为空。adminMaterialListVO {}"+adminMaterialListVO);
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        /**
         * @updator  huangqingpeng
         * 防止有问题的提交会导致数据丢失
         */
        List<EssayMaterialVO> materils = adminMaterialListVO.getMaterialList();
        log.info("saveMaterial|materils|paperId with size:{}->{}",adminMaterialListVO.getPaperId(),materils.size());
        for(EssayMaterialVO essayMaterialVO:materils){
            if(StringUtils.isBlank(essayMaterialVO.getContent())||
                    "".equals(essayMaterialVO.getContent()
                            .replaceAll("<[^>|(img)]*>","")
                            .replace("&nbsp;","")
                            .trim())){
                log.info("材料中存在无内容的材料，请检查后再提交。adminMaterialListVO {}"+adminMaterialListVO);
                throw new BizException(EssayErrors.MATERIAL_NO_CONTENT_ERROR);
            }
        }
        long paperId = adminMaterialListVO.getPaperId();
        //根据试卷id查询材料所有可用材料
        List<Long> oldIdList = essayMaterialRepository.findIdByPaperIdAndStatus(paperId, EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());

        if(CollectionUtils.isNotEmpty(adminMaterialListVO.getMaterialList())){
            LinkedList<Long> newIdList = new LinkedList<>();

            for(EssayMaterialVO materialVO:adminMaterialListVO.getMaterialList()){

                if(null != materialVO.getId() && materialVO.getId() != 0){
                    newIdList.add(materialVO.getId());
                }
            }
            
           if(CollectionUtils.isNotEmpty(oldIdList)){
               //需要删除的idList(差集)
               List<Long> deleteIdList =(List<Long>) CollectionUtils.disjunction(oldIdList, newIdList);
               if(CollectionUtils.isNotEmpty(deleteIdList)){
                   essayMaterialRepository.deleteByList(deleteIdList);
                   //删除试题中对应的材料
                   essayQuestionMaterialRepository.deleteByMaterialIdList(deleteIdList);

               }
           }
            int sort = 0;

            for(EssayMaterialVO materialVO:adminMaterialListVO.getMaterialList()){
                sort += 1;
                String content = materialVO.getContent();
                try {
                    content = htmlFileUtil.imgManage(content,userId,0);
                    content = htmlFileUtil.htmlManage(content);
                } catch (Exception e) {
                    log.error("材料内容处理失败");
                    e.printStackTrace();
                }
                EssayMaterial material = EssayMaterial.builder()
                        .content(content)
                        .paperId(paperId)
                        .sort(sort)
                        .build();
                if(null != materialVO.getId() && 0 != materialVO.getId()){
                    material.setId(materialVO.getId());
                }

                material.setStatus(EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus());
                material.setBizStatus(EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus());

                EssayMaterial save = essayMaterialRepository.save(material);
                essayMaterials.add(save);
            }
        }

        //编辑材料。试卷状态置为（下线，未审核）
        essayPaperBaseRepository.modifyPaperToOffline(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(),EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(),paperId);

        List<Long> materialIdList = essayMaterialRepository.findIdByPaperId(paperId);
        //查询所有关联相关材料的试题
        List<Long> questionBaseIdList = essayQuestionMaterialRepository.findQuestionBaseIdByMaterialIdIn(materialIdList);
        //type =5  清除缓存中的材料
        for(Long questionBaseId:questionBaseIdList){
            /**
             * 清空智能批改材料相关缓存
             */
//            String materialKey = RedisKeyConstant.getMaterialKey(questionBaseId);
//            redisTemplate.delete(materialKey);
//            log.info("清除材料缓存成功，key值:"+materialKey);
            String correctMaterialKey = CorrectRedisKeyConstant.getMaterialQuestionKey(questionBaseId);
            redisTemplate.delete(correctMaterialKey);
            log.info("清除试题材料缓存成功（批改用），key值:"+correctMaterialKey);
        }
        String correctMaterialPaperKey = CorrectRedisKeyConstant.getMaterialPaperKey(paperId);
        redisTemplate.delete(correctMaterialPaperKey);
        log.info("清除试卷材料缓存成功（批改用），key值:"+correctMaterialPaperKey);
        return  essayMaterials;
    }

}
