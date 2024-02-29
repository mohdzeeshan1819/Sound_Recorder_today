package com.shaluambasta.soundrecorder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class MainActivity : AppCompatActivity() {


    private lateinit var imgBackground: ShapeableImageView
    private var currentMediaPlayer: MediaPlayer? = null

    private lateinit var btnStart: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnPlay: MaterialButton
    private val recordings = mutableListOf<Recording>()
    private lateinit var recyclerView: RecyclerView

    private var mediaRecorder: MediaRecorder? = null
    private var recordState: Boolean = false
    private var fileName: String = ""
    private var pauseState: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private var playState: Boolean = false

    private var count: Int = 1


    // Update this function to set the filename with the count
    private fun generateFileName(): String {
        return "${externalCacheDir?.absolutePath}/record$count.mp3"
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val child = findViewById<ImageView>(R.id.child)
        val men = findViewById<ImageView>(R.id.men)
       val ghost = findViewById<ImageView>(R.id.ghost)
        val women = findViewById<ImageView>(R.id.women)

        imgBackground = findViewById(R.id.img_background)
        btnStart = findViewById(R.id.btn_start)
        btnPause = findViewById(R.id.btn_pause)
        btnPlay = findViewById(R.id.btn_play)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        fileName = "${externalCacheDir?.absolutePath}/recording$count.mp3"
        count++


        btnStart.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 0)
            } else {

                onRecord(!recordState)
            }

        }

        btnPause.setOnClickListener {
            pauseRecording()
        }


        btnPlay.setOnClickListener {
            onPlay(!playState)

        }
        child.setOnClickListener(){
            child()
        }
        men.setOnClickListener(){
            men()
        }
        ghost.setOnClickListener(){
            ghost()
        }
        women.setOnClickListener(){
            women()
        }
    }


    private fun child(){
        if(fileName!=null){
        startPlaying(fileName,null, pitch = 1.5f, speed = 1.0f)}
        else{
            startPlaying(null, Uri.parse(fileName), pitch = 1.5f, speed = 1.0f)}

    }


    private fun men(){
        if(fileName!=null){
            startPlaying(fileName,Uri.parse(fileName), pitch = 1.5f, speed = 1.0f)}
        else{
            startPlaying(fileName, Uri.parse(fileName), pitch = 1.5f, speed = 1.0f)}
    }  private fun women(){

        if(fileName!=null){
            startPlaying(fileName,Uri.parse(fileName), pitch = 2.0f, speed = 1.0f)}
        else{
            startPlaying(fileName, Uri.parse(fileName), pitch = 2.5f, speed = 1.0f)}
    }  private fun ghost(){
//        startPlaying(fileName, pitch = 0.5f, speed = 1.0f) // Adjust pitch to 1.2 times higher and speed to 80% of normal speed
        selectAudio()
    }

    fun selectAudio() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Pass the file path, pitch, and speed to the startPlaying function
                    startPlaying(null,uri, pitch = 1.0f, speed = 1.0f)
            }
        }
    }



    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
        setUpRecyclerView()


    }

    private fun startRecording() {
        fileName = generateFileName()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }

        try {

            mediaRecorder!!.prepare()
            mediaRecorder!!.start()

            recordState = !recordState
            "Stop".also { btnStart.text = it }

            btnPause.visibility = View.VISIBLE
            btnPlay.visibility = View.GONE
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()

            // Create a new Recording object for the new recording and add it to the list
            val newRecording = Recording(fileName)
            saveRecording(newRecording)


        } catch (e: Exception) {

            e.printStackTrace()
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()

        }
    }


    private fun stopRecording() {

        if (recordState) {
            mediaRecorder?.apply {

                stop()
                release()
                recordState = !recordState
                "Start".also { btnStart.text = it }
                btnPause.visibility = View.GONE
                btnPlay.visibility = View.VISIBLE

            }
            Toast.makeText(this, "Recording Stopped!", Toast.LENGTH_SHORT).show()
            mediaRecorder = null
        }



    }

    private fun onPlay(start: Boolean) {
        if (start) {
            // Pass the file path of the recording to the startPlaying function
            startPlaying(fileName,null, pitch = 0.5f, speed = 1.0f) // Adjust pitch to 1.2 times higher and speed to 80% of normal speed
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying(filePath: String?, audioUri: Uri?, pitch: Float, speed: Float) {
        stopPlaying() // Stop any currently playing audio

        try {
            currentMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Set data source based on whether filePath or audioUri is provided
                if (filePath != null) {
                    setDataSource(filePath)
                } else if (audioUri != null) {
                    val contentResolver = applicationContext.contentResolver
                    setDataSource(applicationContext, audioUri)
                }

                val playbackParams = PlaybackParams().apply {
                    setPitch(pitch)  // Adjust pitch (1.0f is normal pitch)
                    setSpeed(speed)  // Adjust speed (1.0f is normal speed)
                }

                setPlaybackParams(playbackParams) // Apply pitch and speed adjustments

                prepare()
                start()
            }

            // Save the recording
            if (filePath != null) {
                val recording = Recording(filePath)
                saveRecording(recording)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRecording(recording: Recording) {
        recordings.add(recording)
        recyclerView.adapter?.notifyDataSetChanged() // Notify adapter of dataset change
    }

    private fun stopPlaying() {
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
    private fun setUpRecyclerView() {
        val adapter = RecordingAdapter(recordings,
            onItemClick = { recording ->  startPlaying(fileName,Uri.parse(fileName), pitch = 1.2f, speed = 0.8f) },
            onItemLongClick = { recording -> deleteRecording(recording) }
        )
        recyclerView.adapter = adapter
    }

    private fun deleteRecording(recording: Recording) {
        val position = recordings.indexOf(recording)
        if (position != -1) {
            recordings.removeAt(position)
            recyclerView.adapter?.notifyItemRemoved(position)
            // You might also want to delete the actual file associated with the recording
            count-- // Decrement count when deleting a recording
        }
    }



    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {

        if (recordState) {
            if (!pauseState) {
                mediaRecorder?.pause()
                Toast.makeText(this, "Paused!", Toast.LENGTH_SHORT).show()
                pauseState = true
                "Resume".also { btnPause.text = it }
            } else {
                resumeRecording()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {

        mediaRecorder?.resume()
        Toast.makeText(this, "Resumed!", Toast.LENGTH_SHORT).show()
        "Pause".also { btnPause.text = it }
        pauseState = false

    }

    override fun onStop() {
        super.onStop()
        mediaRecorder?.release()
        mediaRecorder = null

    }
    companion object {
        private const val REQUEST_CODE_PICK_AUDIO = 101
    }


}
