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
@ResponseBody
public String doGetSearch(
    @RequestParam @Size(max = 100) String foo, 
    HttpServletResponse response, 
    HttpServletRequest request) {
  
  // Set secure response headers to prevent XSS
  response.setHeader("Content-Type", "text/plain; charset=UTF-8");
  response.setHeader("X-Content-Type-Options", "nosniff");
  response.setHeader("X-XSS-Protection", "1; mode=block");
  
  // Initialize logger for security monitoring
  Logger logger = LoggerFactory.getLogger(SearchController.class);
  
  String message = "Invalid input";
  
  try {
    // Input validation - only allow alphanumeric characters and basic punctuation
    if (foo == null || !foo.matches("^[a-zA-Z0-9\\s,.'-]{1,100}$")) {
      logger.warn("Invalid search input attempted: {}", foo);
      return Encode.forHtml("Invalid search input. Only alphanumeric characters allowed.");
    }
    
    // REMOVED SpEL parser to prevent Expression Language Injection
    // SpEL allows arbitrary code execution and should not be used with user input
    // Replace with safe string processing
    message = "Search query: " + foo;
    
    logger.info("Search performed with safe input: {}", foo);
    
  } catch (Exception ex) {
    logger.error("Error processing search request", ex);
    message = "An error occurred processing your request";
  }
  
  // Apply HTML encoding to prevent XSS attacks
  return Encode.forHtml(message);
}

    return message.toString();
  }
}
