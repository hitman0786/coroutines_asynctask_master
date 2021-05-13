package com.android.coroutines

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.coroutines.lib.CoroutineAsyncTask
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var count = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.text = "start"
        progressBar.max = 10

        btn.setOnClickListener {
            count =1;
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0

            val myTask = MyTask()
            myTask.cancel()
            myTask.executeAndReturn(10 as Integer)
        }
    }

    inner class MyTask: CoroutineAsyncTask<Integer, Integer, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            output.text = "Task Starting...";
        }
        override fun doInBackground(params: Array<out Integer>): String {
            while (count <= params[0].toInt()) {
                try {
                    Thread.sleep(1000)
                    publishProgress(count as Integer)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                count++
            }
            return "Task Completed."
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressUpdate(result: Array<out Any?>) {
            super.onProgressUpdate(result)
            output.text = "Running..."+ result[0] as Int
            progressBar.progress = result[0] as Int
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            progressBar.visibility = View.GONE
            output.text = result
            btn.text = "Restart"
        }
    }
}
