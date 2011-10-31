/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web.feaures;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author lubos
 */
public class Plugin {
    public String name, version;
    public Date updated = new Date();
    public List<Feature> features = new ArrayList<Feature>();
}
