package com.example.baseapp.ui.main.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.baseapp.databinding.FragmentHomeBinding
import com.example.baseapp.ui.main.viewModel.HomeVM

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeVM
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeVM::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //Memory leak 방지를 위해 바인딩 후 반드시 널처리!!
    }
}