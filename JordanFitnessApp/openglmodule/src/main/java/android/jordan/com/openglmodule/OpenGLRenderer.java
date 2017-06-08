package android.jordan.com.openglmodule;

/**
 * Created by Jordan on 6/7/2017.
 */

public class OpenGLRenderer {
    private static OpenGLRenderer instance = null;

    //Singleton Functions
    protected OpenGLRenderer() {
        // Exists only to defeat instantiation.
    }

    public static OpenGLRenderer getInstance() {
        if (instance == null) {
            instance = new OpenGLRenderer();
        }
        return instance;
    }


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native void init(int width, int height);
    public native void step();

}
