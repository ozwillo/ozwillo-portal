package org.oasis_eu.portal.mockserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * 
 * @author mkalamalami
 *
 */
@RestController
@RequestMapping("/a/userinfo")
public class UserInfoEndpoint {

	
	private List<String> ADRESS_SUBFIELDS = Arrays.asList("country", "locality", "postal_code", "street_address");
	
	private ObjectNode userInfo;

	
    @RequestMapping("")
    public String getUserInfo() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.writeValueAsString(userInfo);
    }
    
    // TODO Handle address
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void postUserInfo(@RequestBody JsonNode data) {
    	Iterator<Entry<String, JsonNode>> elements = data.fields();
    	ObjectNode addressInfo = (ObjectNode) userInfo.get("address");
    	while (elements.hasNext()) {
    		Entry<String, JsonNode> node = elements.next();
    		if (ADRESS_SUBFIELDS.contains(node.getKey())) {
        		JsonNode removed = addressInfo.remove(node.getKey());
        		if (removed != null) {
        			addressInfo.set(node.getKey(), new TextNode(node.getValue().textValue()));
        		}
    		}
    		else {
	    		JsonNode removed = userInfo.remove(node.getKey());
	    		if (removed != null) {
	    			userInfo.set(node.getKey(), new TextNode(node.getValue().textValue()));
	    		}
    		}
    	}
    }

	@PostConstruct
	public void initialize() throws JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		JsonParser parser = factory.createParser("{" + 
    			"    \"address\": {" + 
    			"        \"country\": \"France\"," + 
    			"        \"locality\": \"Valence\"," + 
    			"        \"postal_code\": \"26000\"," + 
    			"        \"street_address\": \"15 rue des chênes\"" + 
    			"    }," + 
    			"    \"birthdate\": \"1982-03-25\"," + 
    			"    \"email\": \"alice@example.com\"," + 
    			"    \"email_verified\": true," + 
    			"    \"family_name\": \"Legrand\"," + 
    			"    \"gender\": \"female\"," + 
    			"    \"given_name\": \"Alice\"," + 
    			"    \"name\": \"Alice Legrand\"," + 
    			"    \"sub\": \"bb2c6f76-362f-46aa-982c-1fc60d54b8ef\"," + 
    			"    \"updated_at\": 1399887616," + 
    			"    \"locale\": \"fr\", " + // added
    			"    \"picture\": \"/img/my/avatar/img-21.png\" " + // added
    			"}");
//    	"{" + 
//    	"    \"address\": {" + 
//    	"        \"country\": \"France\"," + 
//    	"        \"postal_code\": \"75013\"," + 
//    	"        \"region\": \"Ile-de-France\"," + 
//    	"        \"street_address\": \"23 rue Daviel\"" + 
//    	"    }," + 
//    	"    \"email\": \"john.doe@example.net\"," + 
//    	"    \"email_verified\": true," + 
//    	"    \"family_name\": \"Doe\"," + 
//    	"    \"gender\": \"M\"," + 
//    	"    \"name\": \"John Doe\"," + 
//    	"    \"organization_admin\": true," + 
//    	"    \"organization_id\": \"a2342900-f9eb-4d54-bf30-1e0d763ec4af\"," + 
//    	"    \"sub\": \"a399684b-4ea3-49c3-800b-b8a0bf1131cb\"," + 
//    	"    \"updated_at\": 1393235529" + 
//    	"}"
		userInfo = (ObjectNode) parser.readValueAsTree();
	}
}
