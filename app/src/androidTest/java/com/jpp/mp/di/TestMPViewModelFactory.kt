package com.jpp.mp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jpp.mp.screens.main.MainActivityViewModel
import com.jpp.mp.screens.main.RefreshAppViewModel
import io.mockk.mockk

@Suppress("UNCHECKED_CAST")
class TestMPViewModelFactory(private val viewModels: MutableMap<Class<out ViewModel>, ViewModel> = mutableMapOf(),
                             mainViewModel: MainActivityViewModel = MainActivityViewModel(mockk(relaxed = true)),
                             refreshAppViewModel: RefreshAppViewModel = mockk(relaxed = true))
    : ViewModelProvider.Factory {

    init {
        viewModels[MainActivityViewModel::class.java] = mainViewModel
        viewModels[RefreshAppViewModel::class.java] = refreshAppViewModel
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModels[modelClass] as T
    fun addVm(vm: ViewModel) {
        viewModels[vm.javaClass] = vm
    }
}