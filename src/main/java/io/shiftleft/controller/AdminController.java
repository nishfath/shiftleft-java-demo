package io.shiftleft.controller;

import io.shiftleft.model.AuthToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Admin checks login
 */
@Controller
public class AdminController {
  private String fail = "redirect:/";

  // helper
private boolean isAdmin(String auth) {
    try {
        // Use JWT tokens instead of serialized objects to prevent deserialization attacks
        // Parse and validate the JWT token
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(auth)
            .getBody();
        
        // Verify the token hasn't expired
        if (claims.getExpiration().before(new Date())) {
            return false;
        }
        
        // Check if the role claim indicates admin privileges
        String role = claims.get("role", String.class);
        return "ADMIN".equals(role);
        
    } catch (JwtException ex) {
        System.out.println("Invalid JWT token: " + ex.getMessage());
        return false;
    } catch (Exception ex) {
        System.out.println("Error validating token: " + ex.getMessage());
        return false;
    }
}

  }

  //
  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.POST)
  public String doPostPrintSecrets(HttpServletResponse response, HttpServletRequest request) {
    return fail;
  }


  @RequestMapping(value = "/admin/printSecrets", method = RequestMethod.GET)
  public String doGetPrintSecrets(@CookieValue(value = "auth", defaultValue = "notset") String auth, HttpServletResponse response, HttpServletRequest request) throws Exception {

    if (request.getSession().getAttribute("auth") == null) {
      return fail;
    }

    String authToken = request.getSession().getAttribute("auth").toString();
    if(!isAdmin(authToken)) {
      return fail;
    }

    ClassPathResource cpr = new ClassPathResource("static/calculations.csv");
    try {
      byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
      response.getOutputStream().println(new String(bdata, StandardCharsets.UTF_8));
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      // redirect to /
      return fail;
    }
  }

  /**
   * Handle login attempt
   * @param auth cookie value base64 encoded
   * @param password hardcoded value
   * @param response -
   * @param request -
   * @return redirect to company numbers
   * @throws Exception
   */
@RequestMapping(value = "/admin/login", method = RequestMethod.POST)
public String doPostLogin(@CookieValue(value = "auth", defaultValue = "notset") String auth, 
                          @RequestBody String password, 
                          HttpServletResponse response, 
                          HttpServletRequest request) throws Exception {
    String succ = "redirect:/admin/printSecrets";
    
    try {
        // Check if valid auth token already exists
        if (!auth.equals("notset")) {
            if(isAdmin(auth)) {
                request.getSession().setAttribute("auth", auth);
                return succ;
            }
        }
        
        // Validate password input format
        String[] pass = password.split("=");
        if(pass.length != 2) {
            return fail;
        }
        
        // Verify password with constant-time comparison to prevent timing attacks
        if(pass[1] != null && pass[1].length() > 0 && 
           MessageDigest.isEqual(pass[1].getBytes(StandardCharsets.UTF_8), 
                                 "shiftleftsecret".getBytes(StandardCharsets.UTF_8))) {
            
            // Generate JWT token instead of serializing objects
            String jwtToken = Jwts.builder()
                .setSubject("admin")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
            
            // Set secure cookie attributes
            Cookie authCookie = new Cookie("auth", jwtToken);
            authCookie.setHttpOnly(true); // Prevent XSS attacks
            authCookie.setSecure(true); // Only transmit over HTTPS
            authCookie.setPath("/");
            authCookie.setMaxAge(3600); // 1 hour
            response.addCookie(authCookie);
            
            // Store token in session
            request.getSession().setAttribute("auth", jwtToken);
            
            return succ;
        }
        return fail;
    }
    catch (Exception ex) {
        // Log error securely without exposing sensitive information
        System.err.println("Authentication error occurred");
        return fail;
    }
}

  }

  /**
   * Same as POST but just a redirect
   * @param response
   * @param request
   * @return redirect
   */
  @RequestMapping(value = "/admin/login", method = RequestMethod.GET)
  public String doGetLogin(HttpServletResponse response, HttpServletRequest request) {
    return "redirect:/";
  }
}
