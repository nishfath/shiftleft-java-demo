package io.shiftleft.controller;

import io.shiftleft.model.Account;
import io.shiftleft.model.Address;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.shiftleft.data.DataLoader;
import io.shiftleft.exception.CustomerNotFoundException;
import io.shiftleft.exception.InvalidCustomerRequestException;
import io.shiftleft.model.Customer;
import io.shiftleft.repository.CustomerRepository;

import org.springframework.web.util.HtmlUtils;

/**
 * Customer Controller exposes a series of RESTful endpoints
 */

@Configuration
@EnableEncryptableProperties
@PropertySource({ "classpath:config/application-sfdc.properties" })
@RestController
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	Environment env;
	
	private static Logger log = LoggerFactory.getLogger(CustomerController.class);

	@PostConstruct
	public void init() {
		log.info("Start Loading SalesForce Properties");
		log.info("Url is {}", env.getProperty("sfdc.url"));
		log.info("UserName is {}", env.getProperty("sfdc.username"));
		log.info("Password is {}", env.getProperty("sfdc.password"));
		log.info("End Loading SalesForce Properties");
	}

	private void dispatchEventToSalesForce(String event)
			throws ClientProtocolException, IOException, AuthenticationException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(env.getProperty("sfdc.url"));
		httpPost.setEntity(new StringEntity(event));
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(env.getProperty("sfdc.username"),
				env.getProperty("sfdc.password"));
		httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

		CloseableHttpResponse response = client.execute(httpPost);
		log.info("Response from SFDC is {}", response.getStatusLine().getStatusCode());
		client.close();
	}

	/**
	 * Get customer using id. Returns HTTP 404 if customer not found
	 *
	 * @param customerId
	 * @return retrieved customer
	 */
	@RequestMapping(value = "/customers/{customerId}", method = RequestMethod.GET)
	public Customer getCustomer(@PathVariable("customerId") Long customerId) {

		/* validate customer Id parameter */
      if (null == customerId) {
        throw new InvalidCustomerRequestException();
      }

      Customer customer = customerRepository.findOne(customerId);
		if (null == customer) {
		  throw new CustomerNotFoundException();
	  }

	  Account account = new Account(4242l,1234, "savings", 1, 0);
	  log.info("Account Data is {}", account);
	  log.info("Customer Data is {}", customer);

      try {
        dispatchEventToSalesForce(String.format(" Customer %s Logged into SalesForce", customer));
      } catch (Exception e) {
        log.error("Failed to Dispatch Event to SalesForce . Details {} ", e.getLocalizedMessage());

      }

      return customer;
    }

    /**
     * Handler for / loads the index.tpl
     * @param httpResponse
     * @param request
     * @return
     * @throws IOException
     */
      @RequestMapping(value = "/", method = RequestMethod.GET)
      public String index(HttpServletResponse httpResponse, WebRequest request) throws IOException {
	  	ClassPathResource cpr = new ClassPathResource("static/index.html");
	  	String ret = "";
		  try {
			  byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			  ret= new String(bdata, StandardCharsets.UTF_8);
		  } catch (IOException e) {
			  //LOG.warn("IOException", e);
		  }
		  return ret;
      }

      /**
       * Check if settings= is present in cookie
       * @param request
       * @return
       */
      private boolean checkCookie(WebRequest request) throws Exception {
      	try {
			return request.getHeader("Cookie").startsWith("settings=");
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return false;
      }

      /**
       * restores the preferences on the filesystem
       *
       * @param httpResponse
       * @param request
       * @throws Exception
       */
      @RequestMapping(value = "/loadSettings", method = RequestMethod.GET)
      public void loadSettings(HttpServletResponse httpResponse, WebRequest request) throws Exception {
        // get cookie values
        if (!checkCookie(request)) {
          httpResponse.getOutputStream().println("Error");
          throw new Exception("cookie is incorrect");
        }
        String md5sum = request.getHeader("Cookie").substring("settings=".length(), 41);
    	ClassPathResource cpr = new ClassPathResource("static");
    	File folder = new File(cpr.getPath());
		File[] listOfFiles = folder.listFiles();
        String filecontent = new String();
        for (File f : listOfFiles) {
          // not efficient, i know
          filecontent = new String();
          byte[] encoded = Files.readAllBytes(f.toPath());
          filecontent = new String(encoded, StandardCharsets.UTF_8);
          if (filecontent.contains(md5sum)) {
            // this will send me to the developer hell (if exists)

            // encode the file settings, md5sum is removed
            String s = new String(Base64.getEncoder().encode(filecontent.replace(md5sum, "").getBytes()));
            // setting the new cookie
            httpResponse.setHeader("Cookie", "settings=" + s + "," + md5sum);
            return;
          }
        }
      }


  /**
   * Saves the preferences (screen resolution, language..) on the filesystem
   *
   * @param httpResponse
   * @param request
   * @throws Exception
   */
@RequestMapping(value = "/saveSettings", method = RequestMethod.POST)
public void saveSettings(HttpServletResponse httpResponse, WebRequest request) throws Exception {
    // Use POST method instead of GET for state-changing operations
    // "Settings" will be stored in a cookie
    // schema: base64(filename,value1,value2...), md5sum(base64(filename,value1,value2...))
    
    Logger logger = Logger.getLogger(CustomerController.class.getName());
    FileOutputStream fos = null;
    
    try {
        // Validate cookie
        if (!checkCookie(request)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getOutputStream().println("Error: Authentication failed");
            return;
        }

        String settingsCookie = request.getHeader("Cookie");
        if (settingsCookie == null || settingsCookie.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: Missing cookie");
            return;
        }

        String[] cookie = settingsCookie.split(",");
        if (cookie.length < 2) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: Malformed cookie");
            return;
        }

        String base64txt = cookie[0].replace("settings=", "");

        // Validate MD5 checksum
        String cookieMD5sum = cookie[1];
        String calcMD5Sum = DigestUtils.md5Hex(base64txt);
        if (!cookieMD5sum.equals(calcMD5Sum)) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: Invalid MD5 checksum");
            return;
        }

        // Decode and parse settings
        String[] settings = new String(Base64.getDecoder().decode(base64txt)).split(",");
        if (settings.length == 0) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: Empty settings");
            return;
        }

        // Extract and validate filename
        String userProvidedFilename = settings[0];
        
        // Sanitize filename to prevent directory traversal
        String sanitizedFilename = sanitizeFilename(userProvidedFilename);
        if (sanitizedFilename == null || sanitizedFilename.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: Invalid filename");
            return;
        }

        // Validate file extension whitelist
        if (!isAllowedFileExtension(sanitizedFilename)) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getOutputStream().println("Error: File type not allowed");
            return;
        }

        // Get base directory and resolve path securely
        ClassPathResource cpr = new ClassPathResource("./static/");
        Path basePath = Paths.get(cpr.getPath()).normalize().toAbsolutePath();
        Path resolvedPath = basePath.resolve(sanitizedFilename).normalize().toAbsolutePath();

        // Validate that resolved path is within base directory
        if (!resolvedPath.startsWith(basePath)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getOutputStream().println("Error: Access denied - path traversal detected");
            logger.warning("Path traversal attempt detected: " + userProvidedFilename);
            return;
        }

        File file = resolvedPath.toFile();
        
        // Create parent directories if needed with secure permissions
        if (!file.exists()) {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    httpResponse.getOutputStream().println("Error: Failed to create directory");
                    return;
                }
            }
        }

        // Write settings to file
        fos = new FileOutputStream(file, true);
        
        // Extract settings content (skip filename)
        String[] settingsArr = Arrays.copyOfRange(settings, 1, settings.length);
        
        // Write settings content
        fos.write(String.join("\n", settingsArr).getBytes("UTF-8"));
        fos.write(("\n" + cookie[cookie.length - 1]).getBytes("UTF-8"));
        
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.getOutputStream().println("Settings Saved");
        
    } catch (IllegalArgumentException e) {
        httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        httpResponse.getOutputStream().println("Error: Invalid input");
        logger.severe("Invalid input: " + e.getMessage());
    } catch (IOException e) {
        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        httpResponse.getOutputStream().println("Error: Failed to save settings");
        logger.severe("IO error: " + e.getMessage());
    } finally {
        // Ensure resources are closed
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                logger.severe("Failed to close file stream: " + e.getMessage());
            }
        }
    }
}

private String sanitizeFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
        return null;
    }
    
    // Remove any path separators and traversal sequences
    String sanitized = filename.replaceAll("[.]{2,}", "")
                              .replaceAll("[/\\\\]", "")
                              .replaceAll("[\u0000-\u001f]", "")
                              .trim();
    
    // Validate filename pattern - allow only alphanumeric, dash, underscore, and dot
    if (!sanitized.matches("^[a-zA-Z0-9._-]+$")) {
        return null;
    }
    
    // Ensure filename doesn't start with dot
    if (sanitized.startsWith(".")) {
        return null;
    }
    
    return sanitized;
}

private boolean isAllowedFileExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
        return false;
    }
    
    // Whitelist of allowed file extensions
    String[] allowedExtensions = {".txt", ".conf", ".properties", ".json"};
    
    String lowerCaseFilename = filename.toLowerCase();
    for (String extension : allowedExtensions) {
        if (lowerCaseFilename.endsWith(extension)) {
            return true;
        }
    }
    
    return false;
}


  /**
   * Debug test for saving and reading a customer
   *
   * @param firstName String
   * @param lastName String
   * @param dateOfBirth String
   * @param ssn String
   * @param tin String
   * @param phoneNumber String
   * @param httpResponse
   * @param request
   * @return String
   * @throws IOException
   */
  @RequestMapping(value = "/debug", method = RequestMethod.GET)
  public String debug(@RequestParam String customerId,
					  @RequestParam int clientId,
					  @RequestParam String firstName,
                      @RequestParam String lastName,
                      @RequestParam String dateOfBirth,
                      @RequestParam String ssn,
					  @RequestParam String socialSecurityNum,
                      @RequestParam String tin,
                      @RequestParam String phoneNumber,
                      HttpServletResponse httpResponse,
                     WebRequest request) throws IOException{

    // empty for now, because we debug
    Set<Account> accounts1 = new HashSet<Account>();
    //dateofbirth example -> "1982-01-10"
    Customer customer1 = new Customer(customerId, clientId, firstName, lastName, DateTime.parse(dateOfBirth).toDate(),
                                      ssn, socialSecurityNum, tin, phoneNumber, new Address("Debug str",
                                      "", "Debug city", "CA", "12345"),
                                      accounts1);

    customerRepository.save(customer1);
    httpResponse.setStatus(HttpStatus.CREATED.value());
    httpResponse.setHeader("Location", String.format("%s/customers/%s",
                           request.getContextPath(), customer1.getId()));

    return customer1.toString().toLowerCase().replace("script","");
  }

	/**
	 * Debug test for saving and reading a customer
	 *
	 * @param firstName String
	 * @param httpResponse
	 * @param request
	 * @return void
	 * @throws IOException
	 */
	@RequestMapping(value = "/debugEscaped", method = RequestMethod.GET)
	public void debugEscaped(@RequestParam String firstName, HttpServletResponse httpResponse,
					  WebRequest request) throws IOException{
		String escaped = HtmlUtils.htmlEscape(firstName);
		System.out.println(escaped);
		httpResponse.getOutputStream().println(escaped);
	}
	/**
	 * Gets all customers.
	 *
	 * @return the customers
	 */
	@RequestMapping(value = "/customers", method = RequestMethod.GET)
	public List<Customer> getCustomers() {
		return (List<Customer>) customerRepository.findAll();
	}

	/**
	 * Create a new customer and return in response with HTTP 201
	 *
	 * @param the
	 *            customer
	 * @return created customer
	 */
	@RequestMapping(value = { "/customers" }, method = { RequestMethod.POST })
	public Customer createCustomer(@RequestParam Customer customer, HttpServletResponse httpResponse,
								   WebRequest request) {

		Customer createdcustomer = null;
		createdcustomer = customerRepository.save(customer);
		httpResponse.setStatus(HttpStatus.CREATED.value());
		httpResponse.setHeader("Location",
				String.format("%s/customers/%s", request.getContextPath(), customer.getId()));

		return createdcustomer;
	}

	/**
	 * Update customer with given customer id.
	 *
	 * @param customer
	 *            the customer
	 */
	@RequestMapping(value = { "/customers/{customerId}" }, method = { RequestMethod.PUT })
	public void updateCustomer(@RequestBody Customer customer, @PathVariable("customerId") Long customerId,
			HttpServletResponse httpResponse) {

		if (!customerRepository.exists(customerId)) {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
			customerRepository.save(customer);
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}

	/**
	 * Deletes the customer with given customer id if it exists and returns
	 * HTTP204.
	 *
	 * @param customerId
	 *            the customer id
	 */
	@RequestMapping(value = "/customers/{customerId}", method = RequestMethod.DELETE)
	public void removeCustomer(@PathVariable("customerId") Long customerId, HttpServletResponse httpResponse) {

		if (customerRepository.exists(customerId)) {
			customerRepository.delete(customerId);
		}

		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}

}
