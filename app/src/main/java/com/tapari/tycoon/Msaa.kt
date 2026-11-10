package com.tapari.tycoon

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/** Picks the best available ES3 config: 4x MSAA, then 2x, then none. Smooth low-poly edges on every phone. */
class MsaaChooser : GLSurfaceView.EGLConfigChooser {
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        val tries = intArrayOf(4, 2, 0)
        for (depth in intArrayOf(24, 16)) {
            for (s in tries) {
                val spec = if (s > 0) intArrayOf(
                    EGL10.EGL_RENDERABLE_TYPE, 0x40,
                    EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, depth,
                    EGL10.EGL_SAMPLE_BUFFERS, 1, EGL10.EGL_SAMPLES, s,
                    EGL10.EGL_NONE
                ) else intArrayOf(
                    EGL10.EGL_RENDERABLE_TYPE, 0x40,
                    EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, depth,
                    EGL10.EGL_NONE
                )
                val num = IntArray(1)
                if (egl.eglChooseConfig(display, spec, null, 0, num) && num[0] > 0) {
                    val cfgs = arrayOfNulls<EGLConfig>(num[0])
                    if (egl.eglChooseConfig(display, spec, cfgs, num[0], num)) {
                        val c = cfgs[0]
                        if (c != null) return c
                    }
                }
            }
        }
        throw IllegalArgumentException("No suitable EGL config")
    }
}
