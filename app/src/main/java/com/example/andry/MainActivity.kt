 package com.example.andry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import java.io.File
import java.util.*

 class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topAppBar))

        val questionInput = findViewById<TextView>(R.id.QuestionInput)
        val searchButton = findViewById<Button>(R.id.search_button)

        searchButton.setOnClickListener {
            askWolfram(questionInput.text.toString())
        }

        val speakButton = findViewById<Button>(R.id.speak_button)
        speakButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to know?")

            try {
                startActivityForResult(intent, 1)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(
                        applicationContext,
                        "Your device is not supported :(",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

        val answerOutput = findViewById<TextView>(R.id.answer_output)
        val textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {})
        textToSpeech.language = Locale.US

        var speechRequest = 0
        findViewById<FloatingActionButton>(R.id.read_answer).setOnClickListener {
            val answer = answerOutput.text.toString()
            textToSpeech.speak(answer, TextToSpeech.QUEUE_ADD, null, speechRequest.toString())
            speechRequest += 1
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         startActivityForResult(intent, 1)
         if (requestCode == 1) {
             if (resultCode == RESULT_OK && data != null) {
                 val result: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                 val question: String? = result?.get(0)
                 if (question != null) {
                     findViewById<TextView>(R.id.QuestionInput).text = question
                 }
             }
         }
     }

    fun askWolfram(question: String) {
        val wolframAppId = "4978LH-K523EGT6VX" //KEY THAT WAS GENERATED ON WOLFRAM ALPHA
        val engine = WAEngine()
        engine.appID = wolframAppId
        Log.d("Get API", wolframAppId)
        engine.addFormat("plaintext")

        val query = engine.createQuery()
        query.input = question

        val answerText = findViewById<TextView>(R.id.answer_output)
        answerText.text = "Let me think..."

        Thread(Runnable{
            val queryResult = engine.performQuery(query)
            answerText.post {
                if (queryResult.isError) {
                    Log.e("wolfram error", queryResult.errorMessage)
                    answerText.text = queryResult.errorMessage
                } else if (!queryResult.isSuccess) {
                    Log.e("wolfram error", "Sorry, I don't understand. Can you rephrase?")
                    answerText.text = "Sorry, I don't understand. Can you rephrase?"
                } else {
                    for (pod in queryResult.pods) {
                        if (!pod.isError) {
                            for (subpod in pod.subpods) {
                                for (element in subpod.contents) {
                                    if (element is WAPlainText) {
                                        Log.d("wolfram alpha", element.text)
                                        answerText.text = element.text
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).start()

    }

}