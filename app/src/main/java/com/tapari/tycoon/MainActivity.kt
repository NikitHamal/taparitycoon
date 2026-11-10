package com.tapari.tycoon

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private lateinit var game: Game
    private lateinit var audio: AudioFx
    private lateinit var renderer: GameRenderer
    private var glView: GLSurfaceView? = null
    private val hud = mutableStateOf(Hud())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val ctl = WindowInsetsControllerCompat(window, window.decorView)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        game = Game()
        Persist.load(this, game)
        audio = AudioFx(this, game)
        audio.init()
        game.fx = audio
        game.hudCb = { h -> hud.value = h }
        renderer = GameRenderer(game)

        setContent {
            TapariTheme {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            GLSurfaceView(ctx).apply {
                                setEGLContextClientVersion(3)
                                setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                                preserveEGLContextOnPause = false
                                setRenderer(renderer)
                                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                                glView = this
                            }
                        }
                    )
                    GameHud(game, hud.value, audio) { Persist.save(this@MainActivity, game) }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        glView?.onResume()
        renderer.resetClock()
        audio.resume()
    }

    override fun onPause() {
        game.inX = 0f
        game.inY = 0f
        glView?.onPause()
        audio.pause()
        Persist.save(this, game)
        super.onPause()
    }

    override fun onDestroy() {
        audio.release()
        super.onDestroy()
    }
}
