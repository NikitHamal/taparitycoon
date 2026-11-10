package com.tapari.tycoon

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val game = Game()
    private lateinit var audio: AudioFx
    private lateinit var renderer: GameRenderer
    private var glView: GLSurfaceView? = null
    private val hud = mutableStateOf(Hud())
    private val screen = mutableIntStateOf(0)   // 0 loading, 1 menu, 2 playing
    private val progress = mutableFloatStateOf(0f)
    private val message = mutableStateOf("Lighting the stove...")
    private val hasSave = mutableStateOf(false)
    private val pauseSignal = mutableIntStateOf(0)
    @Volatile private var loaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val ctl = WindowInsetsControllerCompat(window, window.decorView)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        game.paused = true
        audio = AudioFx(this, game)
        audio.init()
        game.fx = audio
        game.hudCb = { h -> hud.value = h }
        renderer = GameRenderer(game)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (screen.intValue) {
                    2 -> pauseSignal.intValue = pauseSignal.intValue + 1
                    1 -> finish()
                    else -> {}
                }
            }
        })

        setContent {
            TapariTheme {
                val loadAlpha by animateFloatAsState(if (screen.intValue == 0) 1f else 0f, tween(450), label = "loadfade")
                LaunchedEffect(Unit) { runLoading() }
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
                    if (screen.intValue == 2) {
                        GameHud(game, hud.value, audio, pauseSignal.intValue, onMainMenu = { goMenu() }, saveNow = { Persist.save(this@MainActivity, game) })
                    }
                    if (screen.intValue == 1) {
                        MainMenu(game, audio, hasSave.value, onPlay = { play() }, onNewGame = { newGame() })
                    }
                    if (loadAlpha > 0.01f) {
                        Box(Modifier.fillMaxSize().alpha(loadAlpha)) {
                            LoadingScreen(progress.floatValue, message.value)
                        }
                    }
                }
            }
        }
    }

    /** Warms everything up behind the loading screen so gameplay starts smooth. */
    private suspend fun runLoading() {
        val t0 = SystemClock.uptimeMillis()
        message.value = "Rolling the maida..."
        progress.floatValue = 0.08f
        withContext(Dispatchers.IO) { Persist.load(this@MainActivity, game) }
        loaded = true
        hasSave.value = Persist.hasSave(this)
        progress.floatValue = 0.3f
        message.value = "Tuning the street ambience..."
        var guard = 0
        while (!audio.isReady() && guard < 160) {
            delay(50)
            guard++
            progress.floatValue = 0.3f + 0.3f * minOf(1f, guard / 60f)
        }
        progress.floatValue = 0.65f
        message.value = "Building Kathmandu..."
        guard = 0
        while (renderer.framesDrawn < 4 && guard < 160) {
            delay(50)
            guard++
        }
        progress.floatValue = 0.9f
        message.value = "Steaming the moktu..."
        val elapsed = SystemClock.uptimeMillis() - t0
        if (elapsed < 2400L) delay(2400L - elapsed)
        progress.floatValue = 1f
        message.value = "Ready!"
        delay(350)
        screen.intValue = 1
    }

    private fun play() {
        game.inX = 0f
        game.inY = 0f
        game.paused = false
        renderer.setMenu(false)
        screen.intValue = 2
    }

    private fun newGame() {
        Persist.wipe(this)
        game.newGame()
        hasSave.value = false
        play()
    }

    private fun goMenu() {
        game.inX = 0f
        game.inY = 0f
        Persist.save(this, game)
        game.paused = true
        renderer.setMenu(true)
        hasSave.value = true
        screen.intValue = 1
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
        if (loaded) Persist.save(this, game)
        super.onPause()
    }

    override fun onDestroy() {
        audio.release()
        super.onDestroy()
    }
}
