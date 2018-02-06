package com.manuelvicnt.mathrxjava.main

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.manuelvicnt.mathrxjava.fibonacci.FibonacciProducer
import com.manuelvicnt.mathrxjava.number.NumbersApiHelper
import com.manuelvicnt.mathrxjava.number.NumbersApiService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class MainViewModel : ViewModel() {

    private val numbersApiService = NumbersApiService(NumbersApiHelper.numbersApi)
    private var askForFunFact = false

    // This is insecure, everyone could call onComplete if we don't expose an Observable.
    // To make it more secure, you could use a BehaviorRelay (from Jake Wharton RxRelay library)
    private val viewStateSubject = BehaviorSubject.create<MainViewState>()
    val viewStateObservable: Observable<MainViewState>
        get() = this.viewStateSubject

    // Different ways of doing this, I'm taking a naive approach
    val userActionSubject: PublishSubject<MainUserAction> = PublishSubject.create<MainUserAction>()

    init {
        userActionSubject
                .subscribeOn(Schedulers.computation())
                .subscribe({
                    when (it) {
                        is MainUserAction.Calculate -> {
                            if (it.number <= 0) {
                                viewStateSubject.onNext(MainViewState.WrongInputError)
                            } else {
                                viewStateSubject.onNext(MainViewState.Loading)
                                processCalculation(it)
                            }
                        }
                        is MainUserAction.FunFactEnabled -> {
                            askForFunFact = it.enabled
                        }
                    }
                })
    }

    override fun onCleared() {
        Log.d("MainViewModel", "onCleared")
        userActionSubject.onComplete()
        viewStateSubject.onComplete()
        super.onCleared()
    }

    private fun processCalculation(calculateUserAction: MainUserAction.Calculate) {
        if (askForFunFact) {
            Single.zip(FibonacciProducer.fibonacci(calculateUserAction.number),
                    numbersApiService.getNumberFunFact(calculateUserAction.number),
                    BiFunction<Long, String, Pair<Long, String>> { fibonacci, funFact -> Pair(fibonacci, funFact) })
                    .subscribeOn(Schedulers.computation())
                    .subscribe({ result ->
                        viewStateSubject.onNext(MainViewState.Rendered(result.first, result.second))
                    }, {
                        viewStateSubject.onNext(MainViewState.RequestError)
                    })
        } else {
            FibonacciProducer.fibonacci(calculateUserAction.number)
                    .subscribeOn(Schedulers.computation())
                    .subscribe({ result ->
                        viewStateSubject.onNext(MainViewState.Rendered(result, ""))
                    }, {
                        viewStateSubject.onNext(MainViewState.RequestError)
                    })
        }
    }
}