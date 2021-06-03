package com.cvtracker.vmd.contributor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.Contributor
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ContributorBottomSheet : BottomSheetDialogFragment(), ContributorContract.View {
    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun showContributor(contributors: List<Contributor>) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contributor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}