/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web.feaures;

/**
 *
 * @author lubos
 */
public class Feature {
    public static enum FeatureType { DownloadPlugin, UploadPlugin, ExtractionPlugin, AccountStatusPlugin, SearchPlugin };
    
    public String name, className;
    public String info;
    public FeatureType type;
}
