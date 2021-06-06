package com.cvtracker.vmd.contributor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.ContributorAdapter
import com.cvtracker.vmd.data.Contributor
import com.cvtracker.vmd.extensions.launchWebUrl
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_contributor.*
import timber.log.Timber

class ContributorBottomSheet : BottomSheetDialogFragment(), ContributorContract.View {
    private val presenter: ContributorContract.Presenter by lazy { ContributorPresenter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Design_BottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contributor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadContributors()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.cancel()
    }

    override fun showLoading() {
        contributor_loading.isVisible = true
    }

    override fun hideLoading() {
        contributor_loading.isVisible = false
    }

    override fun showContributor(contributors: List<Contributor>) {
        Timber.d(contributors.joinToString(","))
        contributor_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ContributorAdapter(contributors) { clickContributor ->
                clickContributor.links
                    .map { it.url }
                    .firstOrNull()
                    ?.let { activity?.launchWebUrl(it) }
            }
        }
    }

    companion object {
        const val TAG = "ContributorBottomSheet"
        fun newInstance() = ContributorBottomSheet()
    }
}