package android.jordan.com.openglmodule;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Jordan on 6/7/2017.
 */

public class GLView extends GLSurfaceView {
    //,int depth, int stencil both 0
    public GLView(Context context) {
        super(context);
        setEGLContextFactory(new ContextFactory());
        /* Set the renderer responsible for frame rendering */
        setRenderer(new Renderer());
    }

    private static class Renderer implements GLSurfaceView.Renderer {
        public void onDrawFrame(GL10 gl) {
            OpenGLRenderer.step();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            OpenGLRenderer.init(width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Do nothing.
        }
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }
}
