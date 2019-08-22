package com.jpp.mpcredits

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jpp.mp.common.extensions.getResIdFromAttribute
import com.jpp.mp.common.extensions.withNavigationViewModel
import com.jpp.mp.common.extensions.withViewModel
import com.jpp.mp.common.navigation.Destination
import com.jpp.mpcredits.databinding.CreditsFragmentBinding
import com.jpp.mpcredits.databinding.ListItemCreditsBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.credits_fragment.*
import javax.inject.Inject

class CreditsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewBinding: CreditsFragmentBinding

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.credits_fragment, container, false)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        withViewModel {
            viewStates.observe(viewLifecycleOwner, Observer {
                it.actionIfNotHandled { viewState ->
                    viewBinding.viewState = viewState
                    creditsRv.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = CreditsAdapter(viewState.creditsViewState.creditItems) { withViewModel { onCreditItemSelected(it) } }
                        addItemDecoration(DividerItemDecoration(context, (layoutManager as LinearLayoutManager).orientation))
                    }
                }
            })

            navEvents.observe(viewLifecycleOwner, Observer { navEvent -> reactToNavEvent(navEvent) })

            onInit(
                    movieId = NavigationCredits.movieId(arguments),
                    targetImageSize = resources.getDimensionPixelSize(getResIdFromAttribute(R.attr.mpCreditItemImageSize))
            )
        }

        withNavigationViewModel(viewModelFactory) { destinationReached(Destination.ReachedDestination(NavigationCredits.movieTitle(arguments))) }
    }

    /**
     * Helper function to execute actions with the [CreditsViewModel].
     */
    private fun withViewModel(action: CreditsViewModel.() -> Unit) = withViewModel<CreditsViewModel>(viewModelFactory) { action() }

    private fun reactToNavEvent(navEvent: CreditsNavigationEvent) {
        when (navEvent) {
            is CreditsNavigationEvent.ToPerson -> withNavigationViewModel(viewModelFactory) { navigateToPersonDetails(navEvent.personId, navEvent.personImageUrl, navEvent.personName) }
        }
    }


    class CreditsAdapter(private val credits: List<CreditPerson>, private val selectionListener: (CreditPerson) -> Unit) : RecyclerView.Adapter<CreditsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.list_item_credits,
                            parent,
                            false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindCredit(credits[position], selectionListener)
        }

        override fun getItemCount(): Int = credits.size


        class ViewHolder(private val itemBinding: ListItemCreditsBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            fun bindCredit(credit: CreditPerson, selectionListener: (CreditPerson) -> Unit) {
                itemBinding.viewState = credit
                itemBinding.executePendingBindings()
                itemView.setOnClickListener { selectionListener(credit) }
            }
        }
    }
}