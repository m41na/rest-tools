package works.hop.rest.tools.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * TODO: OIO implementation of reading bytes from a stream. Could this be
 * improved to use NIO?
 *
 */
public class FileDataReader {

    public static interface Callback<T> {

        T process(byte[] bytes, int length);
    }

    public static byte[] readBytes(InputStream str) throws IOException {
        int length = 10240;
        byte[] bytes = new byte[length];
        int read;
        int size = 0;
        while ((read = str.read(bytes, size, length)) > -1) {
            System.out.println("more to read...increasing byte buffer");
            size = size + read;
            bytes = Arrays.copyOf(bytes, size + length);
            System.out.println(String.format("size=%d, read=%d", size, read));
        }
        return Arrays.copyOfRange(bytes, 0, size);
    }

    public static <T> T readBytes(InputStream str, Callback<T> callback) throws IOException {
        byte[] bytes = readBytes(str);
        return callback.process(bytes, bytes.length);
    }

    public static class StringCallback implements Callback<String> {

        @Override
        public String process(byte[] bytes, int length) {
            return new String(bytes, 0, length);
        }
    }

    public static class ByteArrayCallback implements Callback<ByteArrayOutputStream> {

        @Override
        public ByteArrayOutputStream process(byte[] bytes, int length) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
            baos.write(bytes, 0, length);
            return baos;
        }
    }

    public static void main(String... args) throws IOException {
        InputStream str = new ByteArrayInputStream("This is a very random test of reading bytes".getBytes());
        System.out.println(readBytes(str, new StringCallback()));
    }
}
