/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.UploadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.BackgroundWorker;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author lubos
 */
@UploadPluginInfo(name = "RapidShare.com upload", sizeLimit = 1024*1024*1024*100)
public class RapidShareUpload extends UploadPlugin {
    static final long SEGMENT_SIZE = 10*1024*1024;
    
    String md5hash, fileId;
    Date lastCheckedFile;
    long progress, size;

    @Override
    public void processFile(final String filePath) {
        md5hash = (String) getPersistentVariable("md5");
        lastCheckedFile = new Date((Long) getPersistentVariable("lastCheckedFile"));
        fileId = (String) getPersistentVariable("fileId");
        
        final File file = new File(filePath);
        
        size = file.length();
        
        if (md5hash == null) {
            
            BackgroundWorker<String, Integer> bw = new BackgroundWorker<String, Integer>() {

                @Override
                public String doInBackground() throws Exception {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] buf = new byte[1024];
                    FileInputStream is = new FileInputStream(file);

                    int lastProgress = -1;
                    long pos = 0, length = file.length();
                    
                    while (pos < length) {
                        long rd = is.read(buf);
                        md.update(buf);
                        pos += rd;
                        
                        int newProgress = (int) (100.0/length*pos);
                        if (newProgress != lastProgress) {
                            updateProgress(newProgress);
                            lastProgress = newProgress;
                        }
                    }

                    return Hex.encodeHexString(md.digest(new byte[32])).toLowerCase();
                }

                @Override
                public void done() {
                    try {
                        md5hash = get();
                        startUpload();
                    } catch (ExecutionException ex) {
                        Exception orig = (Exception) ex.getCause();
                        setFailed(orig.toString());
                    }
                }

                @Override
                protected void progressUpdated(Integer p) {
                    setMessage("Computing MD5 - "+p+"% done");
                }
                
            };
            
            bw.execute();
        } else
            startUpload();
    }
    
    void startUpload() {
        if (fileId != null && (lastCheckedFile == null || new Date().getTime() - lastCheckedFile.getTime() > 1000*60*60*12)) {
            checkFileState();
            return;
        }
        
        MimePart[] mimeParts;
        String action;
        
        if (progress + SEGMENT_SIZE >= size) {
            mimeParts = new MimePart[3];
            action = "upload";
        } else {
            mimeParts = new MimePart[6];
            mimeParts[3] = new MimePartValue("incomplete", "1");
            mimeParts[4] = new MimePartValue("size", ""+size);
            mimeParts[5] = new MimePartValue("md5hex", md5hash);
            action = "uploadresume";
        }
        
        mimeParts[0] = new MimePartValue("login", Settings.getValue("rapidshare/username", null).toString());
        mimeParts[1] = new MimePartValue("password", Settings.getValue("rapidshare/password", null).toString());
        mimeParts[2] = new MimePartFile("filecontent");
        
        this.startUploadChunk("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub="+action, mimeParts, progress, SEGMENT_SIZE);
    }
    
    void checkFileState() {
        fetchPage("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=checkincomplete&fileid="+fileId, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                try {
                    CharBuffer cb = charsetUtf8.decode(buf);
                    String text = cb.toString();
                    
                    if (text.startsWith("ERROR"))
                        fileId = null;
                    else
                        progress = Long.parseLong(text);
                    
                    lastCheckedFile = new Date();
                    setPersistentVariable("lastCheckedFile", lastCheckedFile.getTime());
                    startUpload();
                } catch (Exception ex) {
                    setFailed(ex.getMessage());
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }
            
        });
    }

    @Override
    public void checkResponse(ByteBuffer uploadResponse, Map<String, String> headers) {
        CharBuffer cb = charsetUtf8.decode(uploadResponse);
        String text = cb.toString();
        
        if (text.startsWith("ERROR")) {
            setFailed(text);
            return;
        }
        
        String[] vars = text.split(",");
        fileId = vars[1];
        
        setPersistentVariable("fileId", fileId);
        
        progress += SEGMENT_SIZE;
        
        if (progress < size)
            startUpload();
    }
    
}
