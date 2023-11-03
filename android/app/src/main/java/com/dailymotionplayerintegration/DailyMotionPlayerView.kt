package com.dailymotionplayerintegration

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.dailymotion.player.android.sdk.Dailymotion
import com.dailymotion.player.android.sdk.PlayerView
import com.dailymotion.player.android.sdk.listeners.PlayerListener
import com.dailymotion.player.android.sdk.webview.error.PlayerError
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.ThemedReactContext

class DailyMotionPlayerView(context: ThemedReactContext?) : FrameLayout(context!!) {

    private var playerId: String = ""
    private var videoId: String = ""
    private var dmPlayer: PlayerView? = null

    private fun getReactContext(): ReactContext? {
        return context as ReactContext
    }

    init {
        inflate(getReactContext(), R.layout.activity_main, this)
    }

    override fun requestLayout() {
        super.requestLayout()

        // This view relies on a measure + layout pass happening after it calls requestLayout().
        // https://github.com/facebook/react-native/issues/4990#issuecomment-180415510
        // https://stackoverflow.com/questions/39836356/react-native-resize-custom-ui-component
        post(measureAndLayout)
    }

    private val measureAndLayout = Runnable {
        measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        this.layout(left, top, right, bottom)
    }

    private fun loadThePlayer(): Any {
        val currentActivity = getReactContext()?.currentActivity

        if (currentActivity != null) {

            // Create and configure the Dailymotion PlayerView
            val playerView = PlayerView(getReactContext()!!)

            val playerContainerView = findViewById<View>(R.id.playerContainerView) as FrameLayout

            if (playerContainerView.layoutParams != null) {
                playerView.layoutParams = playerContainerView.layoutParams
            } else {
                Log.e("--DailymotionPlayer--", "No playerContainerView found")
            }

            return createDailymotionPlayer(
                    context,
                    playerId = playerId!!,
                    playerContainerView = playerContainerView
            )
        }
        Log.e("--DailymotionPlayer--", "Container null")

        return View(context) as PlayerView
    }

    fun createDailymotionPlayer(
            context: Context,
            playerId: String,
            playerContainerView: FrameLayout
    ) {

        Log.d("--DailymotionPlayer--", "createDailymotionPlayer")

        Dailymotion.createPlayer(
                context,
                playerId = playerId,
                playerSetupListener =
                object : Dailymotion.PlayerSetupListener {
                    override fun onPlayerSetupFailed(error: PlayerError) {
                        Log.e(
                                "--DailymotionPlayer--",
                                "Error while creating Dailymotion player: ${error.message}"
                        )
                    }

                    override fun onPlayerSetupSuccess(player: PlayerView) {
                        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        dmPlayer = player
                        playerContainerView.addView(dmPlayer, lp)


                        Log.d(
                                "--DailymotionPlayer--",
                                "Added Dailymotion player ${dmPlayer} to view hierarchy"
                        )
                        runTheVideo()
                    }
                },
                playerListener =
                object : PlayerListener {
                    override fun onFullscreenRequested(playerDialogFragment: DialogFragment) {
                        super.onFullscreenRequested(playerDialogFragment)
                        Log.d("--DailymotionPlayer--", "Enter fullscreen")

                        playerContainerView.layoutParams.height = LayoutParams.MATCH_PARENT
                        playerContainerView.layoutParams.width = LayoutParams.MATCH_PARENT


                        // You might need to handle
                        // this@YourClass.dmPlayer?.notifyFullscreenChanged() here.
                    }

                    override fun onFullscreenExit(playerView: PlayerView) {
                        super.onFullscreenExit(playerView)
                        Log.d("--DailymotionPlayer--", "Exit fullscreen")

                        playerContainerView.layoutParams.height = LayoutParams.MATCH_PARENT
                        playerContainerView.layoutParams.width = LayoutParams.MATCH_PARENT
                        // You might need to handle
                        // this@YourClass.dmPlayer?.notifyFullscreenChanged() here.
                    }
                }
        )
    }

    fun setVideoId(videoId: String) {
        this.videoId = videoId
        Log.d("--DailymotionPlayer--", "Set video id ${this.videoId}")
    }


    fun setPlayerId(playerId: String) {
        this.playerId = playerId
        Log.d("--DailymotionPlayer--", "Set player id ${this.playerId}")
        loadThePlayer()
    }

    fun runTheVideo() {
        dmPlayer!!.loadContent(videoId=videoId)
    }
}