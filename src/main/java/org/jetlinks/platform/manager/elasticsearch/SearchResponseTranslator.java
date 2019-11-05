package org.jetlinks.platform.manager.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.hswebframework.web.api.crud.entity.PagerResult;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bsetfeng
 * @since 1.0
 **/
@Slf4j
public class SearchResponseTranslator {

    public static  <T> Mono<PagerResult<T>> translate(Class<T> clazz, SearchResponse response) {
        long total = response.getHits().getTotalHits();
        List<T> results = Arrays.stream(response.getHits().getHits())
                .map(hit -> JSON.toJavaObject(new JSONObject(hit.getSourceAsMap()), clazz))
                .collect(Collectors.toList());
        return Mono.just(PagerResult.of((int) total, results));
    }
}
