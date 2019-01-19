package hello;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
    public String handleFileUpload1(@RequestParam("base64") String file,
    		RedirectAttributes redirectAttributes) {
    	System.out.println("nikhil");
    	//storageService.store(file);
    	//redirectAttributes.addFlashAttribute("message",
    	//		"You successfully uploaded " + file.getOriginalFilename() + "!");
    	try {
    	byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(file);
    	ByteArrayInputStream baIS=	new ByteArrayInputStream(imageBytes);
    	getTextFromImage(baIS);
    	BufferedImage img = ImageIO.read(baIS);

    	
    	
    	// write the image to a file
    	File outputfile = new File("/Users/300006784/Downloads/image.png");
    	
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(file);
    	return "redirect:/";
    }
    
    private void getTextFromImage(InputStream instream) 
    {
        HttpClient httpclient = HttpClients.createDefault();

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

            if (entity != null) 
            {
                System.out.println(EntityUtils.toString(entity));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
