package icu.ynu.log.evaluation;

import icu.ynu.log.common.ConcurrentLruCache;
import icu.ynu.log.common.LogRecordException;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpEl表达式的解析器,在原有解析器的基础上,增加了对SpEL表达式进行缓存
 * icu.ynu.log.evaluation
 *
 * @author xzwnp
 * 2023/3/1
 * 22:13
 */

public class CachedSpelExpressionParser extends SpelExpressionParser {

    //todo 根据配置文件控制缓存大小
    private final ConcurrentLruCache<String, SpelExpression> cache = new ConcurrentLruCache<>(64);

    @Override
    protected SpelExpression doParseExpression(String expressionString, ParserContext context) throws ParseException {
        SpelExpression spelExpression = cache.get(expressionString);
        if (spelExpression == null) {
            try {
                spelExpression = super.doParseExpression(expressionString, context);
                cache.put(expressionString, spelExpression);
            } catch (SpelParseException e) {
                throw new LogRecordException("SpEL表达式解析出错!原因:" + e.getMessage(), e);
            }
        }
        return spelExpression;
    }
}
