//package com.huatu.ztk.search.service;
//
//import com.google.common.base.Strings;
//import com.huatu.ztk.search.bean.QuestoinSearchBean;
//import com.huatu.ztk.search.bean.SearchResult;
//import com.huatu.ztk.search.es.EsClient;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.highlight.HighlightField;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import javax.swing.text.StyledEditorKit;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import static com.huatu.ztk.search.service.QuestionSearchService.*;
//
///**
// * @author jbzm
// * @date 2018下午1:07
// **/
//@Service
//public class QuestionSearchServiceV2 {
//
//    private static final Logger logger = LoggerFactory.getLogger(QuestionSearchServiceV2.class);
//
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
//        /**
//         * jbzm
//         * 第一种方法展示弃用
//         */
//        //添加权重
////        boolQueryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("stem", keyword).boost(50));
////        boolQueryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("material", keyword).boost(30));
////        boolQueryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("choices", keyword).boost(20));
////        boolQueryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("analysis", keyword).boost(1));
////        boolQueryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("referAnalysis", keyword).boost(1));
//
//      //  boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "stem^100", "material^75", "choices^10", "analysis^1", "referAnalysis^1").minimumShouldMatch("80%"));
//     //只查询题干，材料和选项
//        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword, "stem^100", "material^75", "choices^10").minimumShouldMatch("80%"));
//
//
//        //排除模拟题
//        boolQueryBuilder.mustNot(QueryBuilders.termQuery("year", 3000));
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
//
//
//        /*
//            jbzm 注入es对象
//         */
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
//        /**
//         * jbzm
//         * 在添加高亮的情况下无法使用排序
//         */
//        //添加排序条件
//        //searchRequestBuilder.addSort(SortBuilders.fieldSort("year").order(SortOrder.DESC));
//        //通过数字与运算判断是否加载对应字段
//        if (searchType < 0) {
//            searchRequestBuilder.addHighlightedField("stem");
//            searchRequestBuilder.addHighlightedField("material");
//            searchRequestBuilder.addHighlightedField("choices");
//            searchRequestBuilder.addHighlightedField("analysis");
//            searchRequestBuilder.addHighlightedField("referAnalysis");
//        } else {
//            logger.info("searchType={}", searchType);
//            /**
//             * jbzm
//             * 这里说明一下，之前没有按照材料添加高亮，但是现在需求是老师想要搜索材料还要搜索题目，所以权重问题交给es我这里直接添上
//             * 如果以后需要只针对材料进行搜索，这里可以按照下面的规则做一个if  else判断，并且指定subjectType
//             */
//            if ((searchType & 4) == 4) {
//                searchRequestBuilder.addHighlightedField("stem");
//                searchRequestBuilder.addHighlightedField("material");
//            }
//            if ((searchType & 2) == 2) {
//                searchRequestBuilder.addHighlightedField("choices");
//            }
//            if ((searchType & 1) == 1) {
//                searchRequestBuilder.addHighlightedField("analysis");
//            }
//        }
//        //返回匹配内容的长度,如果超长,则会进行截取
//       //
//        // searchRequestBuilder.setHighlighterFragmentSize(100);
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
//            String material = null;
//            if (highlightFields.containsKey("stem")) {
//                fragment = highlightFields.get("stem").fragments()[0].string();
//                if (highlightFields.containsKey("material")) {
//                    material = highlightFields.get("material").fragments()[0].string();
//                }
//            } else if (highlightFields.containsKey("material")) {
//                material = highlightFields.get("material").fragments()[0].string();
//            } else if (highlightFields.containsKey("analysis")) {
//                fragment = highlightFields.get("analysis").fragments()[0].string();
//            } else if (highlightFields.containsKey("referAnalysis")) {
//                fragment = highlightFields.get("referAnalysis").fragments()[0].string();
//            } else if (highlightFields.containsKey("choices")) {
//                fragment = highlightFields.get("choices").fragments()[0].string();
//            } else {
//                break;
//            }
//
//
//            //将查询结果构建为bean
//            final QuestoinSearchBean questoinSearchBean = QuestoinSearchBean.builder()
//                    .fragment(fragment)
//                    .from(from)
//                    .id(id)
//                    .type(type)
//                    .material(material)
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
