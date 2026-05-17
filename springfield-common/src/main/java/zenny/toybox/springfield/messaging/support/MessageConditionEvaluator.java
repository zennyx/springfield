package zenny.toybox.springfield.messaging.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import zenny.toybox.springfield.messaging.Message;

public class MessageConditionEvaluator {

  private final ExpressionParser parser = new SpelExpressionParser();
  private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

  public Boolean evaluate(String conditionExpression, Message<?> message) {
    Expression expression =
        this.expressionCache.computeIfAbsent(conditionExpression, this.parser::parseExpression);
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("message", message);
    context.setVariable("payload", message.getPayload());
    context.setVariable("headers", message.getHeaders());
    return expression.getValue(context, Boolean.class);
  }
}
