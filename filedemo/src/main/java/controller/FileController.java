package controller;




import payload.UploadFileResponse;
import service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import exception.FileStorageException;
import exception.MyFileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
/*import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;*/

@RestController
@RequestMapping("/")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public ResponseEntity<ArrayList<UploadFileResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
    	log.info("File Storage Location:::" + fileStorageService.fileStorageLocation.toString());
    	ArrayList<String> unzippedFileNames = null;
    	try{
    		unzippedFileNames = fileStorageService.storeFile(file);
    	}
    	catch(FileStorageException ex){
    		log.error("Exception occured:" + ex.getMessage());
    		return ResponseEntity.status(500)
    				.body(null);
    	}
        
        ArrayList<UploadFileResponse> fileDownloadURIs = new ArrayList<>();
        for(String fileName: unzippedFileNames){
        	String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(fileName)
                    .toUriString();
        	String fileDeleteUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/deleteFile/")
                    .path(fileName)
                    .toUriString();
        	fileDownloadURIs.add(new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), fileDeleteUri, file.getSize()));
        }
        return ResponseEntity.ok()
                /*.contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")*/
                .body(fileDownloadURIs);
    }

/*  @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }*/

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
    	Resource resource = null;
    	try{
    		resource = fileStorageService.loadFileAsResource(fileName);
    	}
        catch(MyFileNotFoundException ex){
        	log.info("File Not Found" + fileName);
        	return ResponseEntity.status(404)
        			.body(null);
        }

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @DeleteMapping("/deleteFile/{fileName:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName){
    	String response;
    	try{
    		response = fileStorageService.deleteFile(fileName);
    	}
    	catch(MyFileNotFoundException ex){
    		return ResponseEntity.status(404)
    		.body("file" + fileName + "not found");
    	}
    	catch(Exception ex){
    		return ResponseEntity.status(400)
    				.body("Exception occured");
    	}
    	return ResponseEntity.ok()
    			.body(response);
    }

}
