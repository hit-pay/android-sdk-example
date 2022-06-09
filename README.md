# HitPay Android SDK (Point-Of-Sale Only)

[![License](https://img.shields.io/cocoapods/l/HitPay-iOS-SDK.svg?style=flat)](https://github.com/hit-pay/android-sdk-example)

## About the SDK

This Android SDK is specifically designed for Point-of-Sales apps that wish to integrate Card Reader and Paynow transactions into their application.

NOTE: If you are looking for online payment acceptance please refer to these REST API [docs](https://hit-pay.com/docs.html)

## Example App
To run the example project, clone the repo, and open example app to android studio then run app.

## Functionality

- **Authentication** Allow your merchant to log in to their hitpay account.
- **Connect Card Reader** Allow your merchant to connect their card reader to start accepting card payments
- **Accept Card Payment** Allow your merchant to initiate a payment using the connected card reader
- **Accept PayNow** Allow your merchant to initiate a payment using the PayNow QR code
- **Refund Transaction** Allow merchant to perform a full refund on any transactions


## Installation

HitPay-Android-SDK is available through [Maven](https://repo1.maven.org/maven2/). To install
it, simply add the following line to your Podfile:

1. Add to Top-level build.gradle:

```ruby
buildscript {
    ...
    dependencies {
        ...
        classpath 'com.dipien:bye-bye-jetifier:1.2.1'
    }
}
allprojects {
    repositories {
        ...
        //Hitpay
        maven { url 'https://jitpack.io' }
        maven { url 'https://d37ugbyn3rpeym.cloudfront.net/terminal/android-betas' }

    }
}
```

2. Add to app build.gradle:
```ruby
android {
  buildFeatures {
          dataBinding true
       }
}
dependencies {
    implementation 'com.hit-pay.android:com.hitpay.terminalsdk:0.0.6'
}

```

### Authentication

```java
Hitpay.initiateAuthentication()

// To get authentication listener: 
Hitpay.setHitPayAuthenticationListener(this);

// Sign out
HitPay.signOut()

```

### Set Environment

```java
//To set production environment set it = true, sandbox set it = false, defalt is true (production)
Hitpay.setEnv(true/false);

```

### **Connect Card Reader**

```java

// Check for location permissions and setup
if (Build.VERSION.SDK_INT >= 31) {
                    requestPermissionsIfNecessarySdk31();
                } else {
                    requestPermissionsIfNecessarySdkBelow31();
                }

// Enable or disable terminal simulation, default is disable.
 Hitpay.setSimulatedTerminal(true/false);
          
 // To get Terminal listener: 
Hitpay.setHitPayTerminalListener(this);


------------------------------------------------------------------------------------------

 private void requestPermissionsIfNecessarySdkBelow31() {
        // Check for location permissions
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // If we don't have them yet, request them before doing anything else
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissionLauncher.launch(permissions);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null && !adapter.isEnabled()) {
                    adapter.enable();
                }
            } else {
                Log.w(getClass().getSimpleName(), "Failed to acquire Bluetooth permission");
            }
            if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                Hitpay.setSimulatedTerminal(simulated_switch.isChecked());
                Hitpay.initiateTerminalSetup();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestPermissionsIfNecessarySdk31() {
        // Check for location and bluetooth permissions
        List<String> deniedPermissions = new ArrayList<>();
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            deniedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!isGranted(Manifest.permission.BLUETOOTH_CONNECT))
            deniedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        if (!isGranted(Manifest.permission.BLUETOOTH_SCAN))
            deniedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);

        if (!deniedPermissions.isEmpty()) {
            // If we don't have them yet, request them before doing anything else
            String[] deniedPermissionsArray = new String[deniedPermissions.size()];
            deniedPermissionsArray = deniedPermissions.toArray(deniedPermissionsArray);
            requestPermissionLauncher.launch(deniedPermissionsArray);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null && !adapter.isEnabled()) {
                    adapter.enable();
                }
            } else {
                Log.w(getClass().getSimpleName(), "Failed to acquire Bluetooth permission");
            }
            if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                Hitpay.setSimulatedTerminal(simulated_switch.isChecked());
                Hitpay.initiateTerminalSetup();
            }
        }
    }


// Register the permissions callback to handles the response to the system permissions dialog.
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onActivityResult
    );

    private boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    private void onActivityResult(Map<String, Boolean> result) {
        List<String> deniedPermissions = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (!entry.getValue()) {
                deniedPermissions.add(entry.getKey());
            }
        }

        if (deniedPermissions.isEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null && !adapter.isEnabled()) {
                    adapter.enable();
                }
            } else {
                Log.w(getClass().getSimpleName(), "Failed to acquire Bluetooth permission");
            }
            if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                Hitpay.setSimulatedTerminal(simulated_switch.isChecked());
                Hitpay.initiateTerminalSetup();
            }
        }
    }

```

### Accept Card Payment

```java
Hitpay.makeTerminalPayment(amount: amount, currency: "sgd");

// Cancel current terminal payment, 
HitPay.cancelTerminalPayment()

 // To get Terminal Charge listener: chargeTerminalCompleted(), cancelTerminalPayment()
Hitpay.setHitPayTerminalChargeListener(this);

```

### Accept PayNow QR

```java
Hitpay.makePayNowPayment(amount: amount, currency: "sgd", , generateImage: true);
// "qrCode" represents the string value of the QRCode to be displayed.
// "qrImage" represents UIImage of the QRCode if generateImage set to true
  
  
 // To get Terminal Charge listener: 
Hitpay.setHitPayPayNowChargeListener(this);

```

### Refund Transaction

```java
Hitpay.refundCharge(charge_id: charge_id);

 // To get Refund listener: 
 Hitpay.setHitPayRefundListener(this);

```


## Contact
Support: support@hit-pay.com

Author: 1bannamgiauten

## License

HitPay-Android-SDK is available under the MIT license. See the LICENSE file for more info.
