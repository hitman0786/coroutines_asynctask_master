# coroutines_asynctask_master
Convert asynk task into coroutines 

CoroutinesTask file can be used as AsyncTask
If you want to migrate asynctask with coroutines, you can just replace asynctask with CoroutinesTask
Without much change it will handle all the thinks what AsynkTask do but coroutine way.



**Implementation:-**

For more information check the Demo APP

**Code available in Master Branch******

**Create the Custom Class such as MyTask and extends the CoroutinesAsyncTask and override the method
   :- doInBackground() method is mandatory. It is use to perfrom the task in background thread.
   :-onPreExecute(), onPostExecute() onProgressUpdate() and onCancelled() methods are optional, override if required
   :-Call cancel() method to cancel the Job and remove the handler(Recommended to call it before calling executeAndReturn())
   :-Call isCancelled() to check that Job is canceled or not as per requirement
   :-This lib file can be use with Java and kotlin code**
   
   
  ** Code:-**
  
  val myTask = MyTask()
  myTask.cancel()
  myTask.executeAndReturn(10 as Integer)
  
  
  **Helper class**
  
  class MyTask: CoroutineAsyncTask<Integer, Integer, String>(){
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
