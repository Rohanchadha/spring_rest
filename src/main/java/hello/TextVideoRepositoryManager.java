package hello;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.ManagedBean;

import org.springframework.beans.factory.annotation.Autowired;

@ManagedBean
public class TextVideoRepositoryManager {
	@Autowired
	TextVideoRepository textVideoRepository;
	public List<String> getVideosFromText(List<String> text) {
		List<String> videoUrls = new ArrayList<>();
		for(String s : text) { 
			System.out.println("lines: " + s);
			List<String> url = textVideoRepository.getVideosFromTextNativeQuery(s);
			for(String str: url) {
				if(!videoUrls.contains(str))
					videoUrls.add(str);
				System.out.println("video: "+ str );
			}
	
		}
		
		return videoUrls;
		
	}
	
	public void storeVideosAndText(List<String> text, String url) {
		
		for(String s: text) {
			/*para += "(" + s + "," + s + "," + url + ")";
			if(i != text.size() -1) {
				para += ",";
			}
			i++;*/
			System.out.println("para:" + s);
			try {
				textVideoRepository.storeVideosAndText(s,s,url);
			}catch(Exception e) {
				System.out.println(e);
			}
		}
		
		
		
	}

}
