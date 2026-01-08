package com.kazmi.dev.my.secret.media.ads_consent.google_consent_obj

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.room.concurrent.AtomicBoolean
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object GoogleConsentManager {

    private const val TAG = "consent_info_324786235923"

    private lateinit var consentInfo: ConsentInformation
    private val shouldCheckCanRequestAds: AtomicBoolean = AtomicBoolean(true)

    val canRequestAds: Boolean
        get() = consentInfo.canRequestAds()

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInfo.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun initConsentInfo(
        activity: Activity,
        debugMode: Boolean = false,
        onAdsInitialized: () -> Unit,
        onError: (String) -> Unit
    ) {

        /**Consent Info*/
        consentInfo = UserMessagingPlatform.getConsentInformation(activity)

        /** Consent Request Builder */
        val consentRequest = ConsentRequestParameters.Builder()
            .apply {
                if (debugMode){
                    val debugSettings = ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .addTestDeviceHashedId("39QDt7fVWUuPqLsPDAF3XkuDQEKiZkxN9z")
                        .build()
                    setConsentDebugSettings(debugSettings)
                }
            }
            .build()

        /** Request Consent */
        consentInfo.requestConsentInfoUpdate(
            activity,
            consentRequest,
            {/*handle Success*/

                Log.d(TAG, "initConsentInfo: Consent info gathered Success.")

                if (canRequestAds) {
                    //handle ads initialization
                    Log.d(TAG, "initConsentInfo: Yes can request Ads first check.")
                    initializeAds(activity.applicationContext, onAdsInitialized)
                }

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        //handle Error
                        val errorMessage = formError.message
                        Log.d(TAG, "initConsentInfo: Consent form Error -> $errorMessage")
                        onError(errorMessage)
                        return@loadAndShowConsentFormIfRequired
                    }
                    when(consentInfo.consentStatus){
                        ConsentInformation.ConsentStatus.OBTAINED->{
                            Log.d(TAG, "initConsentInfo: Consent form obtained.")
                            //handle ads initialization
                            if (canRequestAds){
                                initializeAds(activity.applicationContext, onAdsInitialized)
                            }
                        }
                        ConsentInformation.ConsentStatus.REQUIRED->{
                            Log.d(TAG, "initConsentInfo: Consent form required.")
                        }
                        ConsentInformation.ConsentStatus.NOT_REQUIRED->{
                            Log.d(TAG, "initConsentInfo: Consent form not required.")
                        }
                        ConsentInformation.ConsentStatus.UNKNOWN->{
                            Log.d(TAG, "initConsentInfo: Consent form unknown.")
                        }
                    }
                }

            },
            { formError ->  /*handle Error*/
                Log.d(TAG, "initConsentInfo: Consent info gathered Error -> ${formError.message}.")
            }
        )

    }

    private fun initializeAds(
        context: Context,
        onAdsInitialized: () -> Unit,
    ) {
        if (shouldCheckCanRequestAds.getAndSet(false)) {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(context){initializationStatus->

                    //google ads initialization check
                    val statusMap = initializationStatus.adapterStatusMap
                    val googleAdsStatus = statusMap["com.google.android.gms.ads.MobileAds"]

                    googleAdsStatus?.let { status ->
                        when (status.initializationState) {
                            AdapterStatus.State.READY -> {
                                Log.d(TAG, "Google Mobile Ads adapter is READY")
                            }
                            AdapterStatus.State.NOT_READY -> {
                                Log.d(TAG, "Google Mobile Ads adapter is NOT_READY")
                            }
                        }
                        Log.d(TAG, "Description: ${status.description}")
                        Log.d(TAG, "Latency: ${status.latency}ms")
                    }

                }
                Log.d(TAG, "initializeAds: Ads Initialized")
                withContext(Dispatchers.Main) { onAdsInitialized() }
            }
            return
        }
        Log.d(TAG, "initializeAds: Ads Already Initialized (second check)")
    }

    fun showPrivacyOptionForm(activity: Activity){
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError->
            if (formError != null){
                //handle error
                Log.d(TAG, "showPrivacyOptionForm: Error -> ${formError.message}")
            }else{
                Log.d(TAG, "showPrivacyOptionForm: Success")
            }
        }
    }

}
