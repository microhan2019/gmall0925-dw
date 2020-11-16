package com.atguigu.gmall0925.dw.publisher.service.impl;

import com.atguigu.gmall.dw.constant.GmallConstants;
import com.atguigu.gmall0925.dw.publisher.service.RealtimeService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RealtimeServiceImpl implements RealtimeService {

    @Autowired
    JestClient jestClient;

    @Override
    public int getDauTotal(String date) {

       /* //这种方式需要手动拼接，不方便
        String query = "{\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"filter\" : {\n" +
                "        \"match\" : {\n" +
                "          \"logDate\" : {\n" +
                "            \"query\" : \"2020-11-03\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"groupby_hour\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"logHour.keyword\" ,\n" +
                "        \"size\": 24\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";*/

        int total = 0;
        //利用工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("logDate", date);
        boolQueryBuilder.filter(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);


        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstants.ES_INDEX_DAU)
                .addType("_doc").build();

        //执行查询
        try {
            SearchResult searchResult = jestClient.execute(search);
            total = searchResult.getTotal();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total;
    }


    @Override
    public Map getDauHours(String date) {

        Map dauHoursMap = new HashMap(64);

        //利用工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("logDate", date);
        boolQueryBuilder.filter(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        //聚合
        TermsBuilder aggTermsBuilder = AggregationBuilders.terms("groupby_logHour").field("logHour.keyword").size(24);
        searchSourceBuilder.aggregation(aggTermsBuilder);


        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstants.ES_INDEX_DAU).addType("_doc").build();

        try {
            SearchResult searchResult = jestClient.execute(search);
            //获得聚合结果
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_logHour").getBuckets();

            for (TermsAggregation.Entry bucket : buckets) {
                dauHoursMap.put(bucket.getKey(), bucket.getCount());
//                System.out.println(bucket.getKey());
//                System.out.println(bucket.getCount());
//                System.out.println("-----------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dauHoursMap;
    }


    //当日订单总金额
    @Override
    public Double getOrderTotalAmount(String date) {

        /*
        GET gmall0925_order/_search
{
  "query": {
    "bool": {
      "filter": {
        "term": {
          "createDate": "2020-11-14"
        }
      }
    }
  },
    "aggs": {
         "sum_totalAmount": {
           "sum": {
             "field": "totalAmount"
           }
         }
    }
}
         */

        //利用 SearchSourceBuilder,不用手动拼接
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));

        searchSourceBuilder.query(boolQueryBuilder);

        //聚合
        SumBuilder sumBuilder = AggregationBuilders.sum("sum_totalAmount").field("totalAmount");
        searchSourceBuilder.aggregation(sumBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstants.ES_INDEX_ORDER).addType("_doc").build();

        Double orderTotalAmount = 0d;
        try {
            //取得聚合结果
            SearchResult searchResult = jestClient.execute(search);
            orderTotalAmount = searchResult.getAggregations().getSumAggregation("sum_totalAmount").getSum();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return orderTotalAmount;
    }


    //分时统计
    @Override
    public Map<String, Double> getOrderTotalAmountHours(String date) {
        /*
        GET gmall0925_order/_search
{
  "query": {
    "bool": {
      "filter": {
        "term": {
          "createDate": "2020-11-14"
        }
      }
    }
  },

   "aggs": {
     "groupby_createHour": {
       "terms": {
         "field": "createHour",
          "size": 24
       }
       , "aggs": {
         "sum_totalAmount": {
           "sum": {
             "field": "totalAmount"
           }
         }
       }
     }
   }
}
         */

        //过滤
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));
        searchSourceBuilder.query(boolQueryBuilder);

        //聚合
        //先做子聚合
        SumBuilder sumBuilder = AggregationBuilders.sum("sum_totalAmount").field("totalAmount");
        //子聚合放入父聚合中
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_createHour").field("createHour").size(24).subAggregation(sumBuilder);

        searchSourceBuilder.aggregation(termsBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstants.ES_INDEX_ORDER).addType("_doc").build();

        Map<String, Double> map = new HashMap<>();
        try {
            //取得聚合结果
            SearchResult searchResult = jestClient.execute(search);
            //先取分组
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_createHour").getBuckets();

            for (TermsAggregation.Entry bucket : buckets) {
                //取各个分组的结果
                String hour = bucket.getKey();
                Double sum_totalAmount = bucket.getSumAggregation("sum_totalAmount").getSum();
                map.put(hour, sum_totalAmount);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }


    /**
     * 根据日期、关键字、聚合字段得到用户购买商品明细和聚合结果
     *
     * @param date
     * @param keyword
     * @param aggFileName
     * @param aggSize
     * @param startPage   开始页数
     * @param pageSize    行数
     * @return
     */
    @Override
    public Map getSaleDetail(String date, String keyword, String aggFileName, int aggSize, int startPage, int pageSize) {
        if (aggSize == 0) {
            aggSize = 1000;
        }

        //初始化一个Map，存放用户购买商品明细和聚合结果
        Map saleDetailMap = new HashMap();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //按照 日期过滤和关键字匹配
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("dt", date));
        boolQueryBuilder.must(new MatchQueryBuilder("sku_name", keyword).operator(MatchQueryBuilder.Operator.AND));
        searchSourceBuilder.query(boolQueryBuilder);

        //按照某个字段进行聚合
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_" + aggFileName).field(aggFileName).size(aggSize);
        searchSourceBuilder.aggregation(termsBuilder);

        //根据传入参数进行分页
        searchSourceBuilder.size(pageSize); //每页取多少行
        searchSourceBuilder.from((startPage - 1) * pageSize); //从第几页开始（页数->行数）

        System.out.println(searchSourceBuilder.toString());

        //构建ES查询动作
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstants.ES_INDEX_SALE_DETAIL).
                addType("_doc").build();

        Integer total;
        try {
            //查询ES
            SearchResult searchResult = jestClient.execute(search);
            System.out.println("===========================");
            total = searchResult.getTotal(); //总人数

            //获取明细
            List sourceList = new ArrayList(); //存所有的键值对
            //把每一个键值对都放到单独的一个HashMap中
            List<SearchResult.Hit<HashMap, Void>> hitList = searchResult.getHits(HashMap.class);
            for (SearchResult.Hit<HashMap, Void> hit : hitList) {
                HashMap source = hit.source; //一个个键值对
                sourceList.add(source);
            }

            //把总的明细放到saleDetailMap中
            saleDetailMap.put("detail", sourceList);
            System.out.println("明细："+saleDetailMap);


            //获取聚合结果
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_" + aggFileName).getBuckets();
            Map aggsMap = new HashMap();
            for (TermsAggregation.Entry bucket : buckets) {
                aggsMap.put(bucket.getKey(), bucket.getCount());
            }
            //把总的聚合结果放到saleDetailMap中
            saleDetailMap.put("aggs", aggsMap);
            saleDetailMap.put("total",total);
            System.out.println("聚合之后："+saleDetailMap);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return saleDetailMap;

    }


}
