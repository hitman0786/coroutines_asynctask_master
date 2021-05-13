package com.android.coroutines.lib

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class CoroutineAsyncTask<Params, Progress, Result> {
    private val TAG = "CoroutineAsyncTask"
    private var sHandler: InternalHandler? = null
    private val MESSAGE_POST_PROGRESS = 1
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    /**
     * executeAndReturn() use to start the task
     *  Task().executeAndReturn() or  Task().executeAndReturn(value)
     */
    fun executeAndReturn(vararg params: Params): CoroutineAsyncTask<Params, Progress, Result> {
        return executeCoroutineTask(params)
    }

    /**
     * perform the task in background and main thread
     * in series
     */
    private fun executeCoroutineTask(params: Array<out Params>): CoroutineAsyncTask<Params, Progress, Result> {
        onPreExecute()
        val deferredResult = CoroutineScope(Dispatchers.IO).async {
            doInBackground(params)
        }
        Log.i(TAG, "$TAG start executing result")
        callOnPostExecute(deferredResult)
        return this
    }

    /**
     * Get the result from background thread
     * and pass on onPostExecute()
     */
    private fun callOnPostExecute(deferredResult: Deferred<Result>) {
        job = scope.launch {
            try {
                Log.i(TAG, "$TAG start launch")
                val result = deferredResult.await()
                onPostExecute(result)
            } catch (exception: CancellationException) {
                Log.i(TAG, "$TAG ignoring the cancellation exception")
                onCancelled(exception)
            } catch (exception: Exception) {
                onCancelled(exception)
            }
        }
    }

    @WorkerThread
    protected abstract fun doInBackground(params: Array<out Params>): Result

    @MainThread
    protected open fun onPreExecute() {
        Log.i(TAG, "$TAG onPreExecute invoke")
    }

    @MainThread
    protected open fun onPostExecute(result: Result) {
        Log.i(TAG, "$TAG onPostExecute invoke")
        cancel()
    }

    @MainThread
    protected open fun onProgressUpdate(values: Array<out Any?>) {
        Log.i(TAG, "$TAG onProgressUpdate invoke")
    }

    @MainThread
    protected open fun onCancelled() {
        Log.i(TAG, "$TAG onCancelled invoke")
        cancel()
    }

    @MainThread
    protected open fun onCancelled(exception: Exception?) {
        Log.i(TAG, "$TAG onCancelled with exception invoke")
        onCancelled()
    }

    /**
     * Cancel the job if it is in active state
     */
    fun cancel() {
        if (job?.isCancelled == false && job?.isActive == true) {
            job?.cancel()
            //remove the handler after completing the task
            sHandler?.removeCallbacksAndMessages(null)
            Log.i(TAG, "$TAG job has been canceled!!!!!")
        }
    }

    fun isCancelled(): Boolean {
        return job?.isCancelled == true && job?.isActive == false
    }

    private fun getMainHandler(): Handler? {
        synchronized(CoroutineAsyncTask::class.java) {
            if (sHandler == null) {
                sHandler = InternalHandler(Looper.getMainLooper())
            }
            return sHandler
        }
    }

    private fun getHelperHandler(): Handler {
        return getMainHandler() ?: InternalHandler(Looper.getMainLooper())
    }


    /**
     * Publish the progress of task
     */
    @WorkerThread
    protected fun publishProgress(vararg values: Progress) {
        if (!isCancelled()) {
            getHelperHandler().obtainMessage(
                MESSAGE_POST_PROGRESS,
                AsyncTaskResult(this, *values)
            ).sendToTarget()
        }
    }

    /**
     * update the progress of the task
     */
    private inner class InternalHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val result: AsyncTaskResult<*> = msg.obj as AsyncTaskResult<*>
            Log.i(TAG, "$TAG progress update : ${result.mData}")
            result.mTask.onProgressUpdate(result.mData)
        }
    }

    private class AsyncTaskResult<Data>(
        val mTask: CoroutineAsyncTask<*, *, *>,
        vararg data: Data
    ) {
        val mData: Array<Data> = data as Array<Data>
    }
}