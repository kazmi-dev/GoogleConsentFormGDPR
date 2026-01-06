# 📜 Google Consent Form GDPR/EEA Implementation

This is a Simple callback Kotlin-based utility class to easily implement **Google UMP (User Messaging Platform)** Consent Form for GDPR/EEA compliance in your Android apps.

Supports:
- ✅ Consent request using [Google UMP SDK](https://developers.google.com/admob/android/privacy)
- ✅ Support both Object and Hilt (Two way integration)
- ✅ Debug mode for testing in EEA
- ✅ Ad initialization included
- ✅ Callback support to load ads after consent
- ✅ Privacy options support

---

## 🛠️ Prerequisites

- ✅ User Messaging Platform Dependency
- ✅ Dagger/Hilt Integration if using DI

### 1. 📌 Add UMP dependency in your `libs.versions.toml`:

#### Dependency:
```kotlin dsl
ump = { group = "com.google.android.ump", name = "user-messaging-platform", version.ref = "ump_version" }
```

### 2. 📌 Add Dagger/Hilt support (only required for DI projects)

Add following depedencies and plugins to `libs.versions.toml`

Dependencies:
```depdencies
dagger-hilt = { group = "com.google.dagger", name = "hilt-android", version.ref = "daggerHilt_version" }
dagger-ksp = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "daggerHilt_version" }
```
Plugin:
```plugin
hilt = { id = "com.google.dagger.hilt.android", version.ref = "daggerHilt_version" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp_version" }
```
Versions:
```versions
daggerHilt_version = "2.55"
ksp_version = "2.1.0-1.0.29"
```

#### Now add add dependencies and plugins in `build.gradle` for both app and project level as below:
#### App Level:

Plugins:
```plugin
alias(libs.plugins.ksp)
alias(libs.plugins.hilt)
```
Dependencies:
```depdencies
implementation(libs.dagger.hilt)
ksp(libs.dagger.ksp)
```
#### Project Level:
Plugin:
```plugin
alias(libs.plugins.hilt) apply  false
alias(libs.plugins.ksp) apply  false
```

#### 📌 2. Annotate Your Application Class

```anotate
@HiltAndroidApp
class MyApp : Application()
```

#### 📌 3. Update Manifest file

```update
<application
    android:name=".MyApp"
    ... >
</application>
```

## 📜 Initilization
Initialize UMP in your activity as below using DI:

```initialize
@Inject latinit var googleConsentFormManager: GoogleConsentFormManager
```

Initialize UMP in your activity as below using Object:

```initialize
GoogleConsentManager.initConsentInfo(
            activity = this,
            debugMode = true,
            onAdsInitialized = {
                //onAdInitialized
            },
            onError = {error->
                //handle error
            }
        )
```

### 1. Implement UmpCallbacks in your activity or fragment:

```callbacks
class MainActivity : AppCompatActivity(), GoogleConsentFormManager.UmpCallbacks {

    override fun onRequestAds() {
        // Consent is gathered — load ads now
    }

    override fun onConsentFormError(error: String) {
        // Handle any error that occurred
        Log.e("ConsentError", error)
    }
}
```

### 2. Request Consent:

```request
consentManager.setConsentCallbacks(this)
consentManager.gatherConsentIfAvailable(this) { consentGiven ->
    Log.d("ConsentResult", "Consent gathered: $consentGiven")
}
```

### 3. Optional – Show Privacy Options Manually:

```optional
consentManager.showPrivacyOptionsForm(this) { formError ->
    Log.e("PrivacyFormError", "Error: ${formError.message}")
}
```

### 4. Optional – Reset Consent (Debug/Test Mode Only):

```debug cancel
consentManager.consentReset()
```
## 🧪 Debug Mode Setup
To test consent form in debug mode (EEA simulation)

### 1. Generate your hashed device ID using this code:

```debug
val id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
val hashed = MessageDigest.getInstance("MD5").digest(id.toByteArray()).joinToString("") {
    "%02x".format(it)
}
Log.d("DeviceHash", hashed)
```

### 2. Replace "YOUR-DEVICE-HASHED-ID" in the manager class with your hashed ID.

```replace
val debugSettings = ConsentDebugSettings.Builder(it)
    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
    .addTestDeviceHashedId("YOUR-DEVICE-HASHED-ID")
    .build()
```

## ✅ Done!
### You’ve now implemented Google’s UMP Consent Form in your app with clean separation of concerns and Hilt support.


