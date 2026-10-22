package com.example.sequencebox.domain

class OutputTooLargeException(
    message: String = "The sequence becomes too large to display safely. Try fewer terms or smaller values."
) : IllegalArgumentException(message)
