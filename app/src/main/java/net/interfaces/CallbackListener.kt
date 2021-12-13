package net.interfaces

interface CallbackListener {
    fun onSuccess()
    fun onCancel()
    fun onRetry()

}