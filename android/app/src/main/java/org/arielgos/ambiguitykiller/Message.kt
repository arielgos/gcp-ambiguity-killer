package org.arielgos.ambiguitykiller

enum class MessageType {
    USER, SYSTEM
}


data class Message(val message: String?, val type: MessageType)