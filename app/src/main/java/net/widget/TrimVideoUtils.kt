package net.widget

import android.net.Uri
import android.util.Log
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import net.interfaces.OnTrimVideoListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object TrimVideoUtils {
    private val TAG = TrimVideoUtils::class.java.simpleName
    @Throws(IOException::class)
    fun startTrim(
        src: File,
        dst: String,
        startMs: Long,
        endMs: Long,
        callback: OnTrimVideoListener
    ) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "MP4_$timeStamp.mp4"
        val filePath = dst + fileName
        val file = File(filePath)
        file.parentFile.mkdirs()
        Log.e(
            TAG,
            "Generated file path $filePath  $src  $dst  $startMs  $endMs"
        )
        genVideoUsingMp4Parser(src, file, startMs, endMs, callback)
    }

    @Throws(IOException::class)
    private fun genVideoUsingMp4Parser(
        src: File,
        dst: File,
        startMs: Long,
        endMs: Long,
        callback: OnTrimVideoListener
    ) {
        val movie = MovieCreator.build(FileDataSourceViaHeapImpl(src.absolutePath))
        val tracks = movie.tracks
        movie.tracks = LinkedList()
        var startTime1 = (startMs / 1000).toDouble()
        var endTime1 = (endMs / 1000).toDouble()
        Log.e(
            TAG,
            "genVideoUsingMp4Parser:::::::::::::::: $startTime1  $endTime1"
        )
        var timeCorrected = false
        for (track in tracks) {
            if (track.syncSamples != null && track.syncSamples.size > 0) {
                if (timeCorrected) {
                    throw RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.")
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false)
                endTime1 = correctTimeToSyncSample(track, endTime1, true)
                timeCorrected = true
            }
        }
        for (track in tracks) {
            var currentSample: Long = 0
            var currentTime = 0.0
            var lastTime = -1.0
            var startSample1: Long = -1
            var endSample1: Long = -1
            for (i in track.sampleDurations.indices) {
                val delta = track.sampleDurations[i]
                if (currentTime > lastTime && currentTime <= startTime1) {
                    startSample1 = currentSample
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    endSample1 = currentSample
                }
                lastTime = currentTime
                currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
                currentSample++
            }
            movie.addTrack(AppendTrack(CroppedTrack(track, startSample1, endSample1)))
        }
        dst.parentFile.mkdirs()
        if (!dst.exists()) {
            dst.createNewFile()
        }
        val out = DefaultMp4Builder().build(movie)
        val fos = FileOutputStream(dst)
        val fc = fos.channel
        out.writeContainer(fc)
        fc.close()
        fos.close()
        if (callback != null) callback.getResult(Uri.parse(dst.toString()))
    }

    private fun correctTimeToSyncSample(track: Track, cutHere: Double, next: Boolean): Double {
        val timeOfSyncSamples = DoubleArray(track.syncSamples.size)
        var currentSample: Long = 0
        var currentTime = 0.0
        for (i in track.sampleDurations.indices) {
            val delta = track.sampleDurations[i]
            if (Arrays.binarySearch(track.syncSamples, currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.syncSamples, currentSample + 1)] =
                    currentTime
            }
            currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
            currentSample++
        }
        var previous = 0.0
        for (timeOfSyncSample in timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                return if (next) {
                    timeOfSyncSample
                } else {
                    previous
                }
            }
            previous = timeOfSyncSample
        }
        return timeOfSyncSamples[timeOfSyncSamples.size - 1]
    }

    fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val mFormatter = Formatter()
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}
