package lucene.examples;

import java.io.File;

/**
 * Created by yozhao on 5/30/14.
 */
public class Util {
  public static void deleteDir(File file) {
    if (file == null || !file.exists()) {
      return;
    }
    for (File f : file.listFiles()) {
      if (f.isDirectory()) {
        deleteDir(f);
      } else {
        f.delete();
      }
    }
    file.delete();
  }
}
