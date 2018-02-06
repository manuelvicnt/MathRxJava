package com.manuelvicnt.mathrxjava.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.manuelvicnt.mathrxjava.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var viewStateDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        setupViews()
    }

    override fun onStart() {
        super.onStart()
        listenViewModel()
    }

    override fun onStop() {
        if (viewStateDisposable?.isDisposed == false) {
            viewStateDisposable?.dispose()
        }
        super.onStop()
    }

    private fun setupViews() {
        calcButton.setOnClickListener {
            viewModel.userActionSubject.onNext(MainUserAction.Calculate(input.text.toString().toLong()))
        }

        funFact.setOnCheckedChangeListener { _, isChecked ->
            viewModel.userActionSubject.onNext(MainUserAction.FunFactEnabled(isChecked))
        }

    }

    private fun listenViewModel() {
        viewStateDisposable = viewModel.viewStateObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        MainViewState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            result.text = "Loading..."
                            funFactText.text = ""
                        }
                        is MainViewState.Rendered -> {
                            progressBar.visibility = View.GONE
                            result.text = "Fibonacci = ${it.fibonacciNumber.toInt()}"
                            funFactText.text = "${it.funFact}"
                        }
                        MainViewState.WrongInputError -> {
                            showError()
                        }
                        MainViewState.RequestError -> {
                            showError()
                        }
                    }
                })
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        result.text = "Something happened when calculating your input! Sorry!"
        funFactText.text = ""
    }
}
