package android.jordan.com.openglmodule;

/**
 * Created by Scott on 6/7/2017.
 */

public class OpenGLRenderer {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
    public static native void init(int width, int height);
    public static native void step();
}
