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
public String doGetSearch(@RequestParam String foo, HttpServletResponse response, HttpServletRequest request) {
    // Validate input parameter
    if (foo == null || foo.isEmpty()) {
        return "No search term provided";
    }

    // Instead of evaluating user input as SpEL expression, just use it as a search term
    String message = "Search results for: " + Encode.forHtml(foo);
    
    // Return HTML-encoded message to prevent XSS
    return message;
}

    return message.toString();
  }
}
