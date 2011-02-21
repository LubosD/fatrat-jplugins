package info.dolezel.fatrat.plugins.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

class ByteBufferInputStream extends InputStream {

  ByteBuffer buf;
  ByteBufferInputStream( ByteBuffer buf){
      buf.position(0);
      this.buf = buf;
  }
  public synchronized int read() throws IOException {
    if (!buf.hasRemaining())
      return -1;

    int c = buf.get();
    if (c != 0)
        return ((int)c) & 0xff;
    else
        return -1;
  }
}