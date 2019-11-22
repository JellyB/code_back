//package com.huatu.ztk.search.service;
//
//import com.google.common.base.Strings;
//import com.huatu.ztk.commons.CatgoryType;
//import com.huatu.ztk.commons.SubjectType;
//import com.huatu.ztk.knowledge.api.SubjectDubboService;
//import com.huatu.ztk.question.bean.CompositeQuestion;
//import com.huatu.ztk.question.bean.GenericQuestion;
//import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
//import com.huatu.ztk.question.bean.Question;
//import com.huatu.ztk.question.common.QuestionStatus;
//import com.huatu.ztk.search.bean.QuestoinSearchBean;
//import com.huatu.ztk.search.bean.SearchResult;
//import com.huatu.ztk.search.es.EsClient;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.ListenableActionFuture;
//import org.elasticsearch.action.delete.DeleteRequestBuilder;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.index.IndexRequestBuilder;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.percolate.PercolateResponse;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
//import org.elasticsearch.index.query.*;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.highlight.HighlightField;
//import org.jsoup.Jsoup;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by shaojieyue
// * Created time 2016-05-04 16:04
// */
//@Service
//public class QuestionSearchService {
//    private static final Logger logger = LoggerFactory.getLogger(QuestionSearchService.class);
//    /**
//     * 试题es索引
//     */
//    public static final String QUESTION_INDEX_NAME = "questions";
//    /**
//     * 索引类型
//     */
//    public static final String QUESTION_INDEX_TYPE = "question";
//
//    //答题选项
//    public static final String[] optoions = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"};
//
//    /**
//     * 每页最大记录数
//     */
//    public static final int MAX_PAGE_SIZE = 20;
//    public static final int KEYWORD_MAX_LENGTH = 12;
//
//    @Autowired
//    private SubjectDubboService subjectDubboService;
//
//    /**
//     * 索引单个试题
//     * @param question
//     * @return
//     */
//    public boolean index(Question question) {
//        if (question == null) {//为null 不处理
//            logger.warn("question is null,no proccess.");
//            return false;
//        }
//
//        boolean success = true;
//        if (question.getStatus() == QuestionStatus.DELETED) {//删除索引
//            final DeleteRequestBuilder deleteRequestBuilder = EsClient.getInstance().prepareDelete(QUESTION_INDEX_NAME, QUESTION_INDEX_TYPE, String.valueOf(question.getId()));
//            final DeleteResponse deleteResponse = deleteRequestBuilder.execute().actionGet();
//            logger.info("delete document index. result={}", deleteResponse);
//        } else if (question.getStatus() == QuestionStatus.AUDIT_SUCCESS) {//构建索引
//            XContentBuilder builder = null;
//            try {
//                builder = question2builder(question);//构建builder
//            } catch (IOException e) {
//                logger.error("builder index fail.", e);
//                return false;
//            }
//
//            if (builder == null) {
//                return true;
//            }
//            final Client client = EsClient.getInstance();
//            try {
//                /**
//                 * 使用文档ID作为索引中的ID
//                 */
//                IndexRequestBuilder indexRequestBuilder = client.prepareIndex(QUESTION_INDEX_NAME, QUESTION_INDEX_TYPE, String.valueOf(question.getId()));
//                final ListenableActionFuture<IndexResponse> execute = indexRequestBuilder.setSource(builder).execute();
//                logger.info("start getInstance index,question=" + question);
//                final IndexResponse indexResponse;
//                indexResponse = execute.get();
//                logger.info("getInstance index result={}", indexResponse);
//            } catch (Exception e) {
//                logger.error("builder index fail.", e);
//                success = false;
//            }
//        } else {//不需要处理的状态
//            logger.info("question index skip,question status={}", question.getStatus());
//        }
//
//
//        return success;
//    }
//
//    /**
//     * 把question 转为es builder
//     * @param question
//     * @return
//     * @throws IOException
//     */
//    private XContentBuilder question2builder(Question question) throws IOException {
//        XContentBuilder builder = null;
//        if (question instanceof GenericQuestion) {
//            GenericQuestion target = (GenericQuestion) question;
//            builder = XContentFactory.jsonBuilder().startObject()
//                    .field("id", target.getId())//id
//                    .field("type", target.getType())//试题类型
//                    .field("from", StringUtils.trimToEmpty(target.getFrom()))//来源
//                    .field("material", getContentText(target.getMaterial()))//材料
//                    .field("year", target.getYear())
//                    .field("area", target.getArea())
//                    .field("subject", target.getSubject())
//                    .field("catgory", subjectDubboService.getCatgoryBySubject(target.getSubject()))
//                    .field("stem", getContentText(target.getStem()))
//                    .array("choices", wrapperChoices(target.getChoices()))
//                    .field("analysis", getContentText(target.getAnalysis()))
//                    .field("parent", target.getParent())
//                    .field("createTime", target.getCreateTime())
//                    .field("difficult", target.getDifficult())
//                    .field("mode", target.getMode())
//                    .array("points", target.getPoints())
//                    .endObject();
//        } else if (question instanceof CompositeQuestion) {//复合试题不处理
//            CompositeQuestion target = (CompositeQuestion) question;
//            builder = XContentFactory.jsonBuilder().startObject()
//                    .field("id", target.getId())//id
//                    .field("type", target.getType())//试题类型
//                    .field("from", StringUtils.trimToEmpty(target.getFrom()))//来源
//                    .field("stem", getContentText(target.getMaterial()))//材料
//                    .field("year", target.getYear())
//                    .field("area", target.getArea())
//                    .field("createTime", target.getCreateTime())
//                    .endObject();
//        } else if (question instanceof GenericSubjectiveQuestion) {
//            GenericSubjectiveQuestion target = (GenericSubjectiveQuestion) question;
//            builder = XContentFactory.jsonBuilder().startObject()
//                    .field("id", target.getId())//id
//                    .field("type", target.getType())//试题类型
//                    .field("from", StringUtils.trimToEmpty(target.getFrom()))//来源
//                    .field("material", wrapperMaterials(target.getMaterials()))//材料
//                    .field("year", target.getYear())
//                    .field("area", target.getArea())
//                    .field("subject", target.getSubject())
//                    .field("catgory", subjectDubboService.getCatgoryBySubject(target.getSubject()))
//                    .field("stem", getContentText(target.getStem()))
//                    .field("parent", target.getParent())
//                    .field("createTime", target.getCreateTime())
//                    .field("difficult", target.getDifficult())
//                    .field("mode", target.getMode())
//                    .field("referAnalysis", target.getReferAnalysis()) //参考解析
//                    .endObject();
//        } else {
//            throw new IllegalArgumentException("unknow question type. class=" + question.getClass());
//        }
//        return builder;
//    }
//
//    /**
//     * 将内容里面的标签去掉
//     * @param source
//     * @return
//     */
//    private static String getContentText(String source) {
//        if (StringUtils.isBlank(source)) {//为空则直接返回空字符串
//            return "";
//        }
//        //获取html里面的文本内容
//        return Jsoup.parse(source).body().text();
//    }
//
//    /**
//     * 将选项转换为带有A，B，C，D
//     * @param choices
//     * @return
//     */
//    private static String wrapperChoices(List<String> choices) {
//        StringBuilder results = new StringBuilder();
//        for (int i = 0; i < choices.size(); i++) {//遍历选项，把他们拼接到一块
//            results.append(optoions[i]);
//            results.append(":");//：作为选项喝内容的分隔符
//            results.append(getContentText(choices.get(i)));
//            if (i < choices.size() - 1) {//最后一个不用添加空格
//                results.append("&nbsp;");//添加空格
//            }
//        }
//        return results.toString();
//    }
//
//    /**
//     * 转换主观题材料
//     * @param materials
//     * @return
//     */
//    private static String wrapperMaterials(List<String> materials) {
//        return StringUtils.join(materials, "\n");
//    }
//
//
//    /**
//     * 分页搜索关键字
//     * @param keyword      关键字
//     * @param page         页数
//     * @param size         每页大小
//     * @param point        知识点
//     * @param year         试题年份
//     * @param area         区域
//     * @param mode         真题，模拟题
//     * @param questionType 试题类型
//     * @param subject      类目
//     * @param searchType
//     * @return
//     */
//    public SearchResult search(String keyword, int page, int size, int point, int year, int area, int mode, int questionType, int subject, int searchType) {
//        if (size > MAX_PAGE_SIZE) {//检查参数
//            size = MAX_PAGE_SIZE;
//        }
//
//        if (page < 1) {//page <1 的话,则设置为1
//            page = 1;
//        }
//
//        keyword = StringUtils.trimToNull(keyword);
//
//        if (Strings.isNullOrEmpty(keyword)) {//搜索关键字为空
//            return SearchResult.builder().results(new ArrayList<>()).build();
//        }
//
//        if (keyword.length() > KEYWORD_MAX_LENGTH) {//判断关键字,如果超长,则进行截取,防止恶意搜索
//            keyword = keyword.substring(0, KEYWORD_MAX_LENGTH);
//        }
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//创建查询条件
//        //添加权重
//        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "stem^100", "choices^10"));
//       // boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "stem^100", "choices^10", "analysis^1", "referAnalysis^1"));
//
//        if (subject > 0) {
//            boolQueryBuilder.must(QueryBuilders.termQuery("subject", subject));
//        }
//
//
//        if (questionType > 0) {//精确匹配
//            //原则上来说，使用查询语句做全文本搜索或其他需要进行相关性评分的时候，剩下的全部用过滤语句
//            boolQueryBuilder.filter(QueryBuilders.termQuery("type", questionType));
//        }
//
//        if (year > 0) {//年份精确匹配
//            boolQueryBuilder.filter(QueryBuilders.termQuery("year", year));
//        }
//        if (point > 0) {//知识点精确匹配
//            boolQueryBuilder.filter(QueryBuilders.matchQuery("points", point));
//        }
//        if (area > 0) {//地区精确匹配
//            boolQueryBuilder.filter(QueryBuilders.termQuery("area", area));
//        }
//        if (mode > 0) {//试题属性精确匹配
//            boolQueryBuilder.filter(QueryBuilders.termQuery("mode", mode));
//        }
//
//        final Client client = EsClient.getInstance();
//        // 依据查询索引库名称创建查询索引
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(QUESTION_INDEX_NAME).setTypes(QUESTION_INDEX_TYPE);
//
//        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);//设置查询类型
//        searchRequestBuilder.setHighlighterPreTags("<b>");
//        searchRequestBuilder.setHighlighterPostTags("</b>");
//        searchRequestBuilder.setFrom((page - 1) * size).setSize(size);//设置分页信息
//
//        searchRequestBuilder.setQuery(boolQueryBuilder);//设置查询条件
//        searchRequestBuilder.setExplain(false);//设置是否按查询匹配度排序 true:会明显影响查询性能
//
//        //通过数字与运算判断是否加载对应字段
//        if (searchType < 0) {
//            searchRequestBuilder.addHighlightedField("stem");
//            searchRequestBuilder.addHighlightedField("choices");
//            searchRequestBuilder.addHighlightedField("analysis");
//            searchRequestBuilder.addHighlightedField("referAnalysis");
//        } else {
//            logger.info("searchType={}", searchType);
//            if ((searchType & 4) == 4) {
//                searchRequestBuilder.addHighlightedField("stem");
//            }
//            if ((searchType & 2) == 2) {
//                searchRequestBuilder.addHighlightedField("choices");
//            }
//            if ((searchType & 1) == 1) {
//                searchRequestBuilder.addHighlightedField("analysis");
//            }
//        }
//        //返回匹配内容的长度,如果超长,则会进行截取
//        searchRequestBuilder.setHighlighterFragmentSize(100);
//        searchRequestBuilder.setHighlighterNumOfFragments(1);
//        //执行查询
//        SearchResponse response = searchRequestBuilder.execute().actionGet(2, TimeUnit.SECONDS);
//        SearchHits searchHits = response.getHits();
//        long total = searchHits.getTotalHits();
//        SearchHit[] hits = searchHits.getHits();
//        List datas = new ArrayList();
//        for (SearchHit hit : hits) {
//            int id = Integer.valueOf(hit.getId());
//            final Map<String, Object> source = hit.getSource();
//            String from = source.get("from").toString();
//            int type = Integer.valueOf(source.get("type").toString());
//            final Map<String, HighlightField> highlightFields = hit.getHighlightFields();
//            String fragment = null;
//            if (highlightFields.containsKey("stem")){
//                fragment = highlightFields.get("stem").fragments()[0].string();
//            } else if (highlightFields.containsKey("analysis")) {
//                fragment = highlightFields.get("analysis").fragments()[0].string();
//            } else if (highlightFields.containsKey("referAnalysis")) {
//                fragment = highlightFields.get("referAnalysis").fragments()[0].string();
//            } else if(highlightFields.containsKey("choices")){
//                fragment = highlightFields.get("choices").fragments()[0].string();
//            }else{
//                logger.info("没有匹配到：{}",highlightFields.keySet());
//            }
//
//            //将查询结果构建为bean
//            final QuestoinSearchBean questoinSearchBean = QuestoinSearchBean.builder()
//                    .fragment(fragment)
//                    .from(from)
//                    .id(id)
//                    .type(type)
//                    .build();
//            datas.add(questoinSearchBean);
//        }
//
//        //构建返回结果
//        final SearchResult result = SearchResult.builder()
//                .total(total)
//                .currentPage(page)
//                .results(datas)
//                .build();
//        return result;
//    }
//}
