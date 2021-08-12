# HitPay Android SDK (Point-Of-Sale Only)

[![License](https://img.shields.io/cocoapods/l/HitPay-iOS-SDK.svg?style=flat)](https://github.com/hit-pay/android-sdk-example)

## About the SDK

This Android SDK is specifically designed for Point-of-Sales apps that wish to integrate Card Reader and Paynow transactions into their application.

NOTE: If you are looking for online payment acceptance please refer to these REST API [docs](https://hit-pay.com/docs.html)

## Example App
To run the example project, clone the repo, and run `pod install` from the Example directory first.

## Functionality

- **Authentication** Allow your merchant to log in to their hitpay account.
- **Connect Card Reader** Allow your merchant to connect their card reader to start accepting card payments
- **Accept Card Payment** Allow your merchant to initiate a payment using the connected card reader
- **Accept PayNow** Allow your merchant to initiate a payment using the PayNow QR code
- **Refund Transaction** Allow merchant to perform a full refund on any transactions


## Installation

HitPay-Android-SDK is available through [Maven](https://repo1.maven.org/maven2/). To install
it, simply add the following line to your Podfile:

```ruby
// Top-level build.gradle:

allprojects {
    repositories {
        ...
        //Hitpay
        maven { url 'https://jitpack.io' }
        maven { url 'https://d37ugbyn3rpeym.cloudfront.net/terminal/android-betas' }

    }
}
```

```ruby
// app build.gradle:
android {
  buildFeatures {
          dataBinding true
       }
}
dependencies {
    implementation 'com.hit-pay.android:com.hitpay.terminalsdk:0.0.1'
}

```

### Authentication

```java
Hitpay.initiateAuthentication()

// To get authentication listener: 
Hitpay.setHitPayAuthenticationListener(this);

```

### **Connect Card Reader**

```java
// Enable bluetooh in device if it turning off:
if (BluetoothAdapter.getDefaultAdapter() != null &&
                        !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    BluetoothAdapter.getDefaultAdapter().enable();
                }

// Enable or disable terminal simulation, default is disable.
 Hitpay.setSimulatedTerminal(true/false);

// Check for location permissions and setup
if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    if (Hitpay.verifyGpsEnabled(MainActivity.this)) {
                        Hitpay.initiateTerminalSetup();
                    }
                }
          
 // To get Terminal listener: 
Hitpay.setHitPayTerminalListener(this);


```

### Accept Card Payment

```java
Hitpay.makeTerminalPayment(amount: amount, currency: "sgd");

 // To get Terminal Charge listener: 
Hitpay.setHitPayTerminalChargeListener(this);

```

### Accept PayNow QR

```java
Hitpay.makePayNowPayment(amount: amount, currency: "sgd");

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
