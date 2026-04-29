package com.example.naroo.infrastructure.web

import com.example.naroo.infrastructure.web.dto.APiWrappedResponseDto
import com.example.naroo.infrastructure.web.dto.ErrorResponseDto

interface ErrorCode {
    val value: String
}

fun <T : ErrorCode> T.toWrappedDto(): APiWrappedResponseDto<ErrorResponseDto> = APiWrappedResponseDto.error(this.value)
