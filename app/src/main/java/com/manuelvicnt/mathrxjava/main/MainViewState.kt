package com.manuelvicnt.mathrxjava.main

sealed class MainViewState {
    object Loading : MainViewState()
    class Rendered(val fibonacciNumber: Long, val funFact: String) : MainViewState()
    object WrongInputError : MainViewState()
    object RequestError : MainViewState()
}
