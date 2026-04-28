package com.example.naroo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NarooApplication

fun main(args: Array<String>) {
    runApplication<NarooApplication>(*args)
}
