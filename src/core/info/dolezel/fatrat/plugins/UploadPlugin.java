/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2010 Lubos Dolezel <lubos a dolezel.info>

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

/**
 *
 * @author lubos
 */
public abstract class UploadPlugin extends TransferPlugin {
    public abstract void processFile(String filePath);
    public abstract void checkResponse(ByteBuffer uploadResponse);

    protected static class MimePart {
        public String name;

        private MimePart() {
        }
    }
    protected static class MimePartValue extends MimePart {
        public String value;

        public MimePartValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
    protected static class MimePartFile extends MimePart {
        public MimePartFile(String name) {
            this.name = name;
        }
    }

    protected native void startUpload(String url, List<MimePart> mimeParts);
    protected native void putDownloadLink(String url);
}
