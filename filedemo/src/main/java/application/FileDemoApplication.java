package application;

/*
 * author @I349085
 * 
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;


import controller.FileController;
import property.FileStorageProperties;
import service.FileStorageService;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class 
})
@ComponentScan(basePackageClasses = {
		FileController.class,
		FileStorageService.class
})
public class FileDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileDemoApplication.class, args);
	}
	
	
	
	
}