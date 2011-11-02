/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2011 Lubos Dolezel <lubos a dolezel.info>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.ConfigDialog;
import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import info.dolezel.fatrat.plugins.util.XmlUtils;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@DownloadPluginInfo(name = "CZshare.com premium download", regexp = "http://(www\\.)?czshare\\.com/(\\d+/.+|download_file\\.php\\?id=(\\d+)&file=(.+))", forceSingleTransfer = false)
@ConfigDialog("czshare.xml")
public class CzshareDownload extends DownloadPlugin {
    static final Pattern reOldUrl = Pattern.compile("http://(www\\.)?czshare\\.com/download_file\\.php\\?id=(\\d+)&file=(.+)");
    static final Pattern reNewUrl = Pattern.compile("http://(www\\.)?czshare\\.com/(\\d+)/(.+)/(.+)");
    static final Map<Long,String> convertedLinks = new HashMap<Long,String>();

    String link, username, password, linkCode;
    long linkId;

    @Override
    public void processLink(String link) {
        Matcher mOld = reOldUrl.matcher(link);
        if (mOld.matches()) {
            String fileName = mOld.group(3);
            link = "http://czshare.com/" + mOld.group(2) + "/" + fileName;
            linkId = Long.parseLong(mOld.group(2));
            reportFileName(fileName);
        } else {
            Matcher mNew = reNewUrl.matcher(link);
            if (mNew.matches()) {
                linkId = Long.parseLong(mNew.group(2));
                linkCode = mNew.group(3);
                reportFileName(mNew.group(4));
            } else {
                setFailed("Unsupported link");
            }
        }

        this.link = link;

        username = (String) Settings.getValue("czshare/username", null);
        password = (String) Settings.getValue("czshare/password", null);

        if (isEmpty(username) || isEmpty(password)) {
            setFailed("Premium account information required");
            return;
        }

        getLink(true);
    }

    private void getLink(boolean login) {
        fetchPage("http://czshare.com/mulup/show_profi_links.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                try {
                    Document doc = XmlUtils.loadDocument(buf);

                    Node error = XmlUtils.xpathNode(doc, "/new_up/error");
                    if (error != null)
                        throw new Exception(error.getTextContent());

                    synchronized(convertedLinks) {
                        NodeList list = XmlUtils.xpathNodeList(doc, "/new_up/soubor");
                        convertedLinks.clear();
                        
                        for (int i = 0; i < list.getLength(); i++) {
                            Node n = list.item(i);
                            long id = Long.parseLong(XmlUtils.xpathString(n, "id/text()"));
                            String link = XmlUtils.xpathString(n, "odkaz/text()");

                            System.out.println("ID: "+id+", link: "+link);

                            convertedLinks.put(id, link);
                        }

                        if (convertedLinks.containsKey(linkId)) {
                            startDownload(convertedLinks.get(linkId));
                            return;
                        }
                    }
                    
                    convertLink();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setFailed(ex.toString() + " - " + ex.getMessage());
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }

        }, (login) ? new PostQuery().add("jmeno", username).add("heslo", password).toString() : "");
    }

    private void convertLink() {
        fetchPage("http://czshare.com/mulup/new_link.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                try {
                    Document doc = XmlUtils.loadDocument(buf);
                    Node error = XmlUtils.xpathNode(doc, "/new_up/error");
                    if (error != null)
                        throw new Exception(error.getTextContent());

                    getLink(false);
                } catch (Exception ex) {
                    setFailed(ex.getMessage());
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }

        }, new PostQuery().add("id", Long.toString(linkId)).add("kod", linkCode).toString());
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
