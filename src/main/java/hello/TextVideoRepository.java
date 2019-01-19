package hello;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface TextVideoRepository extends JpaRepository<TextVideo, Integer> {
	
	
		
	@Query(value = "SELECT tv.video_url FROM text_video tv WHERE tv.text like %:lines% ", 
			  nativeQuery = true)
			List<String> getVideosFromTextNativeQuery(
			  @Param("lines") String lines);

	@Modifying
	@Query(value = "INSERT INTO text_video (text, topic, video_url) VALUES (:text, :topic, :url)", 
			nativeQuery = true)
	@Transactional
	        void storeVideosAndText(@Param("text") String text, @Param("topic") String topic, @Param("url") String url );
		
		
	
}
	

