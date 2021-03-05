package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfirmCheckInBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confirm_check_in), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ConfirmCheckInViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentConfirmCheckInBinding by viewBindingLazy()
    private val args by navArgs<ConfirmCheckInFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { viewModel.onClose() }
            confirmButton.setOnClickListener { viewModel.onConfirmEvent() }
        }

        Timber.tag("DEEP").i(args.encodedEvent)
        viewModel.decodeEvent(args.encodedEvent)
        viewModel.navigationEvents.observe2(this) { navEvent ->
            Timber.tag("DEEP").i(navEvent.toString())
            when (navEvent) {
                ConfirmCheckInNavigation.BackNavigation -> popBackStack()
                ConfirmCheckInNavigation.ConfirmNavigation -> popBackStack() // TODO Do something else
            }
        }

        // TODO bind data to actual UI
        viewModel.eventData.observe2(this) {
            with(binding) {
                eventGuid.text = "GUID: %s".format(it.guid)
                startTime.text = "Start time: %s".format(it.start)
                endTime.text = "End time: %s".format(it.end)
                description.text = "Description: %s".format(it.description)
            }
        }
    }
}
