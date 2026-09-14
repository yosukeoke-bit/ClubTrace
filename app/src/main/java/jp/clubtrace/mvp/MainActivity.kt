package jp.clubtrace.mvp

import android.graphics.Bitmap
import android.graphics.PointF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import jp.clubtrace.mvp.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var uri: Uri? = null
    private var retriever: MediaMetadataRetriever? = null
    private var durationMs: Long = 0
    private var fps: Double = 30.0
    private var currentFrame: Long = 0

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { picked ->
            if (picked != null) openVideo(picked)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!OpenCVLoader.initLocal()) {
            binding.status.text = "OpenCVの初期化に失敗しました"
            return
        }

        binding.selectVideo.setOnClickListener {
            pickVideo.launch(arrayOf("video/*"))
        }

        binding.setHead.setOnClickListener {
            binding.overlay.tapEnabled = true
            binding.status.text = "クラブヘッドの中心をタップしてください"
        }

        binding.overlay.onTapPoint = { p ->
            binding.overlay.tapEnabled = false
            binding.overlay.setTrajectory(listOf(p))
            binding.status.text =
                "ヘッド位置を指定しました。次は追跡処理を接続します。"
        }

        binding.prevFrame.setOnClickListener {
            currentFrame = (currentFrame - 1).coerceAtLeast(0)
            renderCurrentFrame()
        }
        binding.nextFrame.setOnClickListener {
            currentFrame += 1
            renderCurrentFrame()
        }
    }

    private fun openVideo(newUri: Uri) {
        uri = newUri
        retriever?.release()
        retriever = MediaMetadataRetriever().apply {
            setDataSource(this@MainActivity, newUri)
        }

        durationMs = retriever?.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull() ?: 0L

        fps = retriever?.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE
        )?.toDoubleOrNull() ?: 30.0

        currentFrame = 0
        binding.status.text = "動画読込完了: ${"%.1f".format(fps)} fps"
        renderCurrentFrame()
    }

    private fun renderCurrentFrame() {
        val r = retriever ?: return
        val tUs = ((currentFrame / fps) * 1_000_000.0).roundToLong()
        val maxUs = durationMs * 1000
        val clamped = tUs.coerceIn(0, maxUs)

        val bmp: Bitmap? = r.getFrameAtTime(
            clamped,
            MediaMetadataRetriever.OPTION_CLOSEST
        )
        binding.frameView.setImageBitmap(bmp)
        binding.status.text =
            "Frame $currentFrame / ${(clamped / 1_000_000.0).let { "%.3f".format(it) }} s"
    }

    override fun onDestroy() {
        retriever?.release()
        super.onDestroy()
    }
}
