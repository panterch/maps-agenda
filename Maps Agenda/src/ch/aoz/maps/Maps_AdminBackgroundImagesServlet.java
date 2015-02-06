package ch.aoz.maps;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.*;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

@SuppressWarnings("serial")
public class Maps_AdminBackgroundImagesServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String response = null;
      switch (req.getParameter("type")) {
        case "serve_image":
        	serveBackground(req.getParameter("blob_key"));
          break;
        case "serve_color":
        	serveColor(req.getParameter("color"));
        	break;
        case "delete":
        	if (deleteBackground(req.getParameter("blob_key"))) {
        		response = "{\"result\" : \"true\"}";
        	} else {
        		response = "{\"result\" : \"false\"}";
        	}
          break;
        case "thumbnails":
        	response = getThumbnails();
        	break;
        case "color":
        	response = "{\"color\" : \"" + BackgroundColor.fetchFromStore().getColor() + "\"}";
        	break;
        case "get_upload_url":
        	response = "{\"url\" : \"" + getUploadUrl(req.getParameter("redirect")) + "\"}";
        	break;
      }
      if (response == null) {
        
      }
      resp.setContentType("application/json");
      resp.getWriter().println(response);
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
    	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    	ImagesService imagesService = ImagesServiceFactory.getImagesService();

    	Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
    	List<BlobKey> blobKeys = blobs.get("backgroundImage");

    	if (blobKeys != null && !blobKeys.isEmpty()) {
    		ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKeys.get(0));
    		BackgroundImage image = new BackgroundImage(blobKeys.get(0).getKeyString(), imagesService.getServingUrl(options));
    		image.addToStore();
    	}
  		resp.sendRedirect("/admin2/looknfeel.html");
    }

    private void serveBackground(String blobKey) {
    	BlobKey key = new BlobKey(blobKey);
    	ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(key);
    	ImagesService imagesService = ImagesServiceFactory.getImagesService();
    	BackgroundImage background_image = new BackgroundImage(blobKey, imagesService.getServingUrl(options));
    	background_image.addToStore();
    }
    
    private boolean deleteBackground(String blobKey) {
    	BackgroundImage current_image = BackgroundImage.fetchFromStore();
    	if (current_image.getKey().equals(blobKey))
    		return false;
    	
    	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    	BlobKey key = new BlobKey(blobKey);
    	blobstoreService.delete(key);
    	return true;
    }
    
    private String getThumbnails() {
    	// Fetch the current background image. It will not be editable while being served.
    	BackgroundImage current_image = BackgroundImage.fetchFromStore();
    	
    	StringBuilder response = new StringBuilder();
    	ImagesService imagesService = ImagesServiceFactory.getImagesService();
      Iterator<BlobInfo> iterator = new BlobInfoFactory().queryBlobInfos();
      response.append("{ \"background_thumbnails\": [");
      while(iterator.hasNext()){
    	  BlobKey nextBlobKey = iterator.next().getBlobKey();
    	  ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(nextBlobKey);
    	  response.append("{");
    	  response.append("\"url\": \"").append(imagesService.getServingUrl(options)).append("\", ");
    	  response.append("\"key\": \"").append(nextBlobKey.getKeyString()).append("\", ");
  	  	response.append("\"served\": \"").append(current_image.getKey().equals(nextBlobKey.getKeyString())).append("\"");
    	  response.append("},");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      response.append("]}");
      return response.toString();
    }
    
    private String getUploadUrl(String redirect) {
    	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    	return blobstoreService.createUploadUrl(redirect);
    }
    
    private void serveColor(String color) {
    	BackgroundColor background_color = new BackgroundColor(color);
    	background_color.addToStore();
    }
}
