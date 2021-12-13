package net.interfaces

interface OnProgressVideoListener {
    fun updateProgress(time: Int, max: Int, scale: Float)
}