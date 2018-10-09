package service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import exception.FileStorageException;
import exception.MyFileNotFoundException;
import property.FileStorageProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileStorageService {

    public final Path fileStorageLocation;
    public FileStorageProperties fileStorageProperties;
    
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public ArrayList<String> storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        ArrayList<String> unzippedFileNames = new ArrayList<>();
        log.info("FileName:::" + fileName);
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation/*.resolve(fileName)*/;
            log.info("Target Location:::" + targetLocation.toString());
            //Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            unzippedFileNames = unZip(file, targetLocation);
            return unzippedFileNames;
        } catch (FileStorageException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    
    
    
    private ArrayList<String> unZip(MultipartFile file, Path targetLocation){
    	ArrayList<String> unzippedFileNames = new ArrayList<>();
    	String targetFileLocation = targetLocation.toAbsolutePath().toString();
    	File dir = new File(targetFileLocation);
    	if(!dir.exists()) dir.mkdirs();
    	
    	byte[] buffer = new byte[1024];
    	
    	try{
    		ZipInputStream zis = new ZipInputStream(file.getInputStream());
    		ZipEntry ze = zis.getNextEntry();	
    		while(ze!=null){
    			String fileName = ze.getName();
    			unzippedFileNames.add(fileName);
    			File newFile = new File(targetFileLocation + File.separator + fileName);
    			log.info("Unzipping:" + fileName);
    			new File(newFile.getParent()).mkdirs();
    			FileOutputStream fos = new FileOutputStream(newFile);
    			int len;
    			while((len = zis.read(buffer))>0){
    				fos.write(buffer, 0, len);
    			}
    			fos.close();
    			zis.closeEntry();
    			ze = zis.getNextEntry();
    		}
    		zis.closeEntry();
    		zis.close();
    	}
    	catch(IOException ex){
    		ex.printStackTrace();
    	}
    	return unzippedFileNames;
    }
    
    
    public String deleteFile(String fileName){
    	try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.delete(filePath.toAbsolutePath());
            return "success";
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    	catch(Exception ex){
    		ex.printStackTrace();
    	}
		return "Failed to delete";
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

