import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

public class Compress {
    public byte[] compress(String str) throws Exception {
        try (var obj = new ByteArrayOutputStream();
                var gzip = new GZIPOutputStream(obj);) {
            gzip.write(str.getBytes());
            return obj.toByteArray();
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] compressed = new Compress().compress("pineapple");
        System.out.println(Arrays.toString(compressed));
    }
}
