package com.kazmi.dev.my.secret.media.ads_consent.google_consent_di

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleConsentManager@Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object{
        private const val TAG = "consent_info_324786235923"
    }

    private lateinit var consentInfo: ConsentInformation
    private var shouldCheckCanRequestAds: Boolean = true

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

        /** Consent Request Builder */
        val consentRequest = ConsentRequestParameters.Builder()
            .apply {
                if (debugMode) {
                    val debugSettings = ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .build()
                    setConsentDebugSettings(debugSettings)
                }
            }
            .build()

        /**Consent Info*/
        consentInfo = UserMessagingPlatform.getConsentInformation(activity)

        /** Request Consent */
        Log.d(TAG, "initConsentInfo: Consent info gathering started.")
        consentInfo.requestConsentInfoUpdate(
            activity,
            consentRequest,
            {/*handle Success*/

                Log.d(TAG, "initConsentInfo: Consent info gathered Success.")

                if (consentInfo.canRequestAds()) {
                    //handle ads initialization
                    Log.d(TAG, "initConsentInfo: Yes can request Ads first check.")
                    shouldCheckCanRequestAds = false
                    initializeAds(onAdsInitialized)
                }

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        //handle Error
                        val errorMessage = formError.message
                        Log.d(TAG, "initConsentInfo: Consent form Error -> $errorMessage")
                        onError(errorMessage)
                    } else {
                        //handle ads initialization
                        Log.d(TAG, "initConsentInfo: Consent form Success.")
                        if (shouldCheckCanRequestAds) {
                            Log.d(TAG, "initConsentInfo: Yes can request Ads second check.")
                            initializeAds(onAdsInitialized)
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
        onAdsInitialized: () -> Unit,
    ) {
        MobileAds.initialize(context) {
            //load required ads
            Log.d(TAG, "initializeAds: Ads Initialized")
            onAdsInitialized()
        }
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