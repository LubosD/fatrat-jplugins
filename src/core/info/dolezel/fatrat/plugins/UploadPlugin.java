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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lubos
 */
public abstract class UploadPlugin extends TransferPlugin {
    /**
     * Called by the application to start the upload
     * @param filePath Path of a local file
     */
    public abstract void processFile(String filePath);

    /**
     * Called to perform checks whether the upload was OK and to extract
     * the download link.
     * @param uploadResponse Body of the server response
     * @param headers HTTP headers
     */
    public abstract void checkResponse(ByteBuffer uploadResponse, Map<String,String> headers);

    /**
     * A generic MIME part with a name
     */
    protected static class MimePart {
        /**
         * HTTP form field name
         */
        public String name;

        private MimePart() {
        }
    }

    /**
     * A HTML form field value (input type=text,hidden...)
     */
    protected static class MimePartValue extends MimePart {
        /**
         * HTTP form field value
         */
        public String value;

        public MimePartValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * The file to be uploaded.
     * This subclass exists only so that the application would know
     * under what field name the file should be uploaded.
     */
    protected static class MimePartFile extends MimePart {
        public MimePartFile(String name) {
            this.name = name;
        }
    }

    /**
     * Call to initiate the upload process.
     * @param url URL where the data should be HTTP POSTed
     * @param mimeParts MIME parts of the request
     */
    protected native void startUpload(String url, List<MimePart> mimeParts);

    /**
     * Gives the user the link for the file that has been uploaded.
     * Should be called from checkResponse()
     * @param url Download URL
     */
    protected native void putDownloadLink(String url);
}
