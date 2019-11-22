package ParameterTest;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.util.List;

public class GetAllParameter {
    public static <T, D> List<T> test(List<D> list,Object object) throws NoSuchMethodException, IllegalAccessException, InstantiationException {
        List<T> tList = Lists.newLinkedList();
        for (D d : list) {
        T t= (T) object.getClass().newInstance();
            BeanUtils.copyProperties(d, t);
            tList.add(t);
        }
        return tList;
    }
    @Test
    public void test2() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        EssayQuestionVO essayQuestionVO=new EssayQuestionVO();
        essayQuestionVO.setLimitTime(99);
        List<EssayQuestionBase> essayQuestionBases=Lists.newArrayList();
        List<EssayQuestionVO> essayQuestionVOS=Lists.newArrayList();
        for(int i=1;i<10;i++){
            EssayQuestionBase essayQuestionBase=new EssayQuestionBase();
            essayQuestionBase.setLimitTime(i);
            essayQuestionBases.add(essayQuestionBase);
        }
        for(EssayQuestionVO essayQuestionVO1:essayQuestionVOS){
            System.out.println(essayQuestionVO1.getLimitTime());
            System.out.println("================");
        }
        essayQuestionVOS=test(essayQuestionBases,essayQuestionVO);
        System.out.println(essayQuestionVOS);
        for(EssayQuestionVO essayQuestionVO1:essayQuestionVOS){
            System.out.println(essayQuestionVO1.getLimitTime());
        }
    }
}
