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
    // Initialize logger for security monitoring
    Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    // Default safe message
    String message = "Invalid search parameter";
    
    try {
        // Input validation: Check if parameter is null or empty
        if (foo == null || foo.trim().isEmpty()) {
            logger.warn("Empty or null search parameter received from IP: {}", request.getRemoteAddr());
            return "Search parameter cannot be empty";
        }
        
        // Input validation: Whitelist approach - only allow alphanumeric and safe characters
        Pattern safePattern = Pattern.compile("^[a-zA-Z0-9\\s._-]{1,100}$");
        if (!safePattern.matcher(foo).matches()) {
            logger.warn("Invalid search parameter detected from IP: {}. Parameter: {}", 
                       request.getRemoteAddr(), Encode.forJava(foo));
            return "Invalid search parameter format";
        }
        
        // Sanitize input by encoding special characters
        String sanitizedInput = Encode.forHtml(foo);
        
        // Instead of using SpEL expression evaluation which allows code injection,
        // perform a safe search operation (e.g., database query, simple string matching)
        // Example: Replace dangerous SpEL evaluation with safe string processing
        message = performSafeSearch(sanitizedInput);
        
        // Log successful search for audit purposes
        logger.info("Search performed successfully for parameter: {}", sanitizedInput);
        
    } catch (Exception ex) {
        // Proper exception handling without exposing sensitive information
        logger.error("Error during search operation from IP: {}", request.getRemoteAddr(), ex);
        return "An error occurred while processing your search";
    }
    
    return message;
}

// Helper method to perform safe search operations
private String performSafeSearch(String searchTerm) {
    // Implement your safe search logic here
    // Example: Query database using parameterized queries
    // or perform simple string operations
    return "Search results for: " + searchTerm;
}

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
