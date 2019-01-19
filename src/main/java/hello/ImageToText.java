package hello;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ImageToText 
{
    public static void main(String[] args) 
    {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://centralindia.api.cognitive.microsoft.com/vision/v1.0/recognizeText");

            //builder.setParameter("mode", "{string}");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "e8548287207f40a58f60970d370cf268");


            // Request body
            StringEntity reqEntity = new StringEntity("{\"url\":\"https://wiproconsumercare.com/wp-content/uploads/2016/09/The-Times-of-India-Pg-17-Sep-232016-Bangalore.jpg\"}");
            //InputStreamEntity reqEntity = new InputStreamEntity(instream);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            System.out.println("***********: "+response.getHeaders("Operation-Location")[0].getValue());
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
            		System.out.println(lines);
            		//System.out.println(response1.getEntity().getContent());

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
    }
}

