package hello;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class GreetingController {
	
	@Autowired
	TextVideoRepositoryManager textVideoRepositoryManager;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="hello") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
    
    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

//        model.addAttribute("files", storageService.loadAll().map(
//                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                        "serveFile", path.getFileName().toString()).build().toString())
//                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = null;//storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/a")
    public String handleFileUpload(@RequestParam("pic") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        //storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }
    
    @PostMapping("/")
    public List<String> handleFileUpload1(@RequestParam("base64") String file,
			RedirectAttributes redirectAttributes) {

		byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(file);
		ByteArrayInputStream baIS = new ByteArrayInputStream(imageBytes);
		return getTextFromImage(baIS);
	}
    
    private List<String> getTextFromImage(InputStream instream) 
    {
        HttpClient httpclient = HttpClients.createDefault();
        List<String> textList = new ArrayList<>();
      
        try
        {
            URIBuilder builder = new URIBuilder("https://centralindia.api.cognitive.microsoft.com/vision/v1.0/recognizeText");

            //builder.setParameter("mode", "{string}");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            //request.setHeader("Content-Type", "application/json");
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "e8548287207f40a58f60970d370cf268");


            // Request body
            //StringEntity reqEntity = new StringEntity("{\"url\":\"https://wiproconsumercare.com/wp-content/uploads/2016/09/The-Times-of-India-Pg-17-Sep-232016-Bangalore.jpg\"}");
            InputStreamEntity reqEntity = new InputStreamEntity(instream);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            System.out.println(response.getHeaders("Operation-Location")[0]);
            HttpEntity entity = response.getEntity();
            URIBuilder builder1 = new URIBuilder(response.getHeaders("Operation-Location")[0].getValue());

            //builder.setParameter("mode", "{string}");

            URI uri1 = builder1.build();
            HttpGet request1 = new HttpGet(uri1);
            request1.setHeader("Content-Type", "application/json");
            request1.setHeader("Ocp-Apim-Subscription-Key", "e8548287207f40a58f60970d370cf268");
            List<String> lines = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
            		Thread.sleep(5000);
            		HttpResponse response1 = httpclient.execute(request1);
            		String responseString = EntityUtils.toString(response1.getEntity(), "UTF-8");
            		JSONObject jsonObj = new JSONObject(responseString);
            		if(jsonObj.get("status").toString().equals("Succeeded")) {
            			JSONArray linesArr = jsonObj.getJSONObject("recognitionResult").getJSONArray("lines");
            			for(int k = 0; k < linesArr.length(); k++) {
            				lines.add(linesArr.getJSONObject(k).getString("text"));
            			}
            		}
            		if(!lines.isEmpty()) {
            			return textVideoRepositoryManager.getVideosFromText(lines);
            		}
            }
            if (entity != null) 
            {
                System.out.println(EntityUtils.toString(entity));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return textList;
    }
    
    @PostMapping("/store")
    public String storeImageAndText(@RequestParam("base64") String file, @RequestParam String url) {
    	List<String> textList = new ArrayList<>();
    	try {
    	byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(file);
    	ByteArrayInputStream baIS=	new ByteArrayInputStream(imageBytes);
    	textList = getTextFromImage(baIS);
    	textVideoRepositoryManager.storeVideosAndText(textList, url);
		System.out.println("stored successfully");
		
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	return null;
    }
}
