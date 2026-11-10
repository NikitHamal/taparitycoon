package com.tapari.tycoon

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** OpenGL ES 3.0 renderer. Everything is pre-allocated: the render loop makes no garbage. */
class GameRenderer(private val game: Game) : GLSurfaceView.Renderer {
    private val lit = LitBatch(220000, 460000)
    private val sg = SpriteBatch(1400)
    private val sb = SpriteBatch(1600)
    private val so = SpriteBatch(500)
    private val scene = Scene(game)

    private var litProg = 0
    private var sprProg = 0
    private var uVP = 0
    private var uSunDir = 0
    private var uSunCol = 0
    private var uSky = 0
    private var uGround = 0
    private var sVP = 0
    private var sTex = 0

    private val proj = FloatArray(16)
    private val view = FloatArray(16)
    private val vp = FloatArray(16)
    private var aspect = 0.5f
    @Volatile private var last = 0L
    private var camX = 0f
    private var camZ = 0f
    private var camInit = false
    private var dim = 1f
    private var menuBlend = 1f
    private var menuTarget = 1f
    @Volatile var framesDrawn = 0

    fun setMenu(on: Boolean) {
        menuTarget = if (on) 1f else 0f
    }

    private val sunX: Float
    private val sunY: Float
    private val sunZ: Float

    init {
        val l = sqrt(0.45f * 0.45f + 0.8f * 0.8f + 0.4f * 0.4f)
        sunX = 0.45f / l
        sunY = 0.8f / l
        sunZ = 0.4f / l
    }

    fun resetClock() {
        last = 0L
    }

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        litProg = Gl.program(Gl.LIT_VS, Gl.LIT_FS)
        uVP = GLES30.glGetUniformLocation(litProg, "uVP")
        uSunDir = GLES30.glGetUniformLocation(litProg, "uSunDir")
        uSunCol = GLES30.glGetUniformLocation(litProg, "uSunCol")
        uSky = GLES30.glGetUniformLocation(litProg, "uSky")
        uGround = GLES30.glGetUniformLocation(litProg, "uGround")
        sprProg = Gl.program(Gl.SPR_VS, Gl.SPR_FS)
        sVP = GLES30.glGetUniformLocation(sprProg, "uVP")
        sTex = GLES30.glGetUniformLocation(sprProg, "uTex")
        Atlas.build()
        lit.initGl()
        sg.initGl()
        sb.initGl()
        so.initGl()
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)
        last = 0L
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        aspect = width.toFloat() / height.toFloat()
    }

    override fun onDrawFrame(unused: GL10?) {
        val now = System.nanoTime()
        var dt = if (last == 0L) 0.016f else (now - last) / 1_000_000_000f
        last = now
        if (dt > 0.05f) dt = 0.05f
        game.update(dt)

        val loc = game.loc
        val dimTarget = if (game.darkT > 0f) 0.5f else 1f
        dim += (dimTarget - dim) * min(1f, dt * 2f)
        val cc = Theme.CLEAR[loc]
        val cd = 0.45f + 0.55f * dim
        GLES30.glClearColor(cc[0] * cd, cc[1] * cd, cc[2] * cd, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // ---- camera ----
        menuBlend += (menuTarget - menuBlend) * min(1f, dt * 2.0f)
        val mb = menuBlend
        val yawPlay = 0.38f
        val yaw = yawPlay + (0.38f + 0.5f * sin(game.time * 0.12f) - yawPlay) * mb
        game.camYaw = yaw
        val pitch = 0.98f + (0.78f - 0.98f) * mb
        val tx = (game.px + game.pvx * 0.2f).coerceIn(-7.5f, 11.5f)
        val tz = (game.pz - 1.0f + game.pvz * 0.2f).coerceIn(-6.0f, 9.5f)
        if (!camInit) {
            camX = tx; camZ = tz; camInit = true
        }
        val k = min(1f, dt * 4.5f)
        camX += (tx - camX) * k
        camZ += (tz - camZ) * k
        var shx = 0f
        var shz = 0f
        if (game.shake > 0f) {
            shx = sin(game.time * 90f) * 0.12f * game.shake * 4f
            shz = cos(game.time * 77f) * 0.1f * game.shake * 4f
        }
        val ar = if (aspect < 0.3f) 0.3f else aspect
        val distScale = (0.5f / ar).coerceIn(0.8f, 1.3f)
        val dist = 21.5f * distScale * (1f - mb) + 38f * mb
        val fx = camX * (1f - mb) + 1.5f * mb
        val fz = camZ * (1f - mb) + 1.0f * mb
        val cp = cos(pitch)
        val ex = fx + sin(yaw) * cp * dist
        val ey = sin(pitch) * dist
        val ez = fz + cos(yaw) * cp * dist
        Mat.perspective(proj, 36f, ar, 8f, 190f)
        Mat.lookAt(view, ex, ey, ez, fx + shx, 0f, fz + shz)
        Mat.mul(vp, proj, view)

        synchronized(game.lock) {
            scene.draw(lit, sg, sb, so, view)
        }

        // ---- lit pass ----
        GLES30.glUseProgram(litProg)
        GLES30.glUniformMatrix4fv(uVP, 1, false, vp, 0)
        GLES30.glUniform3f(uSunDir, sunX, sunY, sunZ)
        val sun = Theme.SUN[loc]
        val sky = Theme.SKYAMB[loc]
        val gnd = Theme.GNDAMB[loc]
        GLES30.glUniform3f(uSunCol, sun[0] * dim, sun[1] * dim, sun[2] * dim)
        GLES30.glUniform3f(uSky, sky[0] * (0.5f + 0.5f * dim), sky[1] * (0.5f + 0.5f * dim), sky[2] * (0.55f + 0.45f * dim))
        GLES30.glUniform3f(uGround, gnd[0] * (0.5f + 0.5f * dim), gnd[1] * (0.5f + 0.5f * dim), gnd[2] * (0.55f + 0.45f * dim))
        lit.flush()

        // ---- sprite passes ----
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDepthMask(false)
        GLES30.glUseProgram(sprProg)
        GLES30.glUniformMatrix4fv(sVP, 1, false, vp, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, Atlas.tex)
        GLES30.glUniform1i(sTex, 0)
        sg.flush()
        sb.flush()
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        so.flush()
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthMask(true)
        GLES30.glDisable(GLES30.GL_BLEND)
        framesDrawn++
    }
}
