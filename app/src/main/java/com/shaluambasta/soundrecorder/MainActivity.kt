package com.shaluambasta.soundrecorder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.io.File

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
    private var isAudioAdded = false

    private var count: Int = 1
    var commonUri:Uri? =null


    private var audioFilePath: String? = null

    private val audioPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                audioFilePath = uri.toString() // Save the URI of the selected audio file
            }
        }
    }


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
        val folder = findViewById<ImageView>(R.id.folder)


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
//            child()
            mediaPlayer?.stop()
            commonUri?.let { it1 -> song(it1,2.0,1.0) }
        }
        men.setOnClickListener(){
//            men()
            stopPlayback()
            commonUri?.let { it1 -> song(it1,0.8,1.0) 
            }

        }
        ghost.setOnClickListener(){
//                ghost()
            stopPlayback()
            commonUri?.let { it1 -> song(it1,0.7,1.0)
            }

        }
        women.setOnClickListener(){
            mediaPlayer?.stop()
//            women()
            commonUri?.let { it1 -> song(it1,2.5,1.0) }

        }
        folder.setOnClickListener(){
            val intent_upload = Intent()
            intent_upload.setType("audio/*")
            intent_upload.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent_upload, 1)
            setUpRecyclerView()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    private fun song(commonUri:Uri,pitch: Double, speed: Double) {
        mediaPlayer?.stop()
        if (fileName.isNotEmpty()) {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setDataSource(applicationContext, commonUri)
            mediaPlayer.prepare()

            // Adjust pitch and speed
            val playbackParams = PlaybackParams().apply {
                setPitch(pitch.toFloat())  // Adjust pitch
                setSpeed(speed.toFloat())  // Adjust speed
            }
            mediaPlayer.playbackParams = playbackParams
            mediaPlayer.start()
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==1){

            if (resultCode === RESULT_OK) {

                //the selected audio.
                val uri: Uri? = data?.data

                if(uri != null)
                {
                    commonUri= uri
                }
            }
        }
    }



    private fun child() {
        mediaPlayer?.stop()


        startPlaying(fileName, pitch = 1.5f, speed = 1.0f)
    }
    private fun men(){
        mediaPlayer?.stop()


        startPlaying(fileName, pitch = 1.0f, speed = 1.0f)
    }
    private fun women(){
        mediaPlayer?.stop()


        startPlaying(fileName, pitch = 2.5f, speed = 1.0f)
    }
    private fun ghost(){
        mediaPlayer?.stop()
        startPlaying(fileName, pitch = 0.8f, speed = 1.0f)
    }
    private fun folder(){
        selectAudio()
        playAudio()


    }


    private fun selectAudio() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        audioPickerLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PICK_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PICK_AUDIO)

    }





    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()


    }

    private fun startRecording() {
        fileName = generateFileName()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION) // Set audio source for noise cancellation
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Set appropriate audio encoder
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
        isAudioAdded = false
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
            startPlaying(fileName, pitch = 0.5f, speed = 1.0f) // Adjust pitch to 1.2 times higher and speed to 80% of normal speed
        } else {
            stopPlaying()
        }
        isAudioAdded = false
    }
    private fun playAudio() {
        audioFilePath?.let { path ->
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }
        }
    }

    private fun startPlaying(filePath: String?, pitch: Float, speed: Float) {
        stopPlaying()
        isAudioAdded = false

        try {
            currentMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (filePath != null) {
                    if (File(filePath).exists()) {
                        setDataSource(filePath)
                    } else {
                        // File does not exist, handle error
                        Toast.makeText(this@MainActivity, "Recorded File not found ", Toast.LENGTH_SHORT).show()
                        Log.d("file", File(filePath).exists().toString())
                        return
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Invalid file path", Toast.LENGTH_SHORT).show()
                    return
                }

                val playbackParams = PlaybackParams().apply {
                    setPitch(pitch)
                    setSpeed(speed)
                }

                setPlaybackParams(playbackParams)
                prepare()
                start()
            }

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
        // Check if the recording already exists in the list
        if (!recordings.contains(recording)) {
            recordings.add(recording)
            recyclerView.adapter?.notifyDataSetChanged() // Notify adapter of dataset change
        }
    }


    private fun stopPlaying() {
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
    private fun setUpRecyclerView() {
        val adapter = RecordingAdapter(recordings,
            onItemClick = { recording ->  startPlaying(fileName, pitch = 1.2f, speed = 0.8f) },
            onItemLongClick = { recording -> deleteRecording(recording) }
        )
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()

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
