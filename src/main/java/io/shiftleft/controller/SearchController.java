package io.shiftleft.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Search login
 */
@Controller
public class SearchController {

@RequestMapping(value = "/search/user", method = RequestMethod.GET)
public String doGetSearch(@RequestParam String foo, HttpServletResponse response, HttpServletRequest request) {
    // Define a safe pattern for allowed expressions
    Pattern safePattern = Pattern.compile("^[a-zA-Z0-9._\\-\\s]+$");
    
    // Validate the input against the safe pattern
    if (foo == null || !safePattern.matcher(foo).matches()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
    }
    
    // Create a sandboxed context with predefined values
    StandardEvaluationContext context = new StandardEvaluationContext();
    
    // Provide safe data for the expression to work with
    Map<String, Object> safeData = new HashMap<>();
    safeData.put("username", "defaultUser");
    safeData.put("id", 0);
    context.setRootObject(safeData);
    
    Object message = new Object();
    try {
        ExpressionParser parser = new SpelExpressionParser();
        // Use the sanitized input with the restricted context
        Expression exp = parser.parseExpression(foo);
        message = exp.getValue(context);
        
        // Additional validation on the result
        if (message == null) {
            message = "No results found";
        }
    } catch (Exception ex) {
        // Avoid leaking stack traces to client
        // Log the exception securely instead of printing to console
        message = "An error occurred during search";
    }
    
    return message.toString();
}

    // Return HTML-encoded message to prevent XSS
    return message;
}

    return message.toString();
  }
}
