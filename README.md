# Kindred Prints Android SDK

The Kindred Prints SDK makes it extremely easy to start selling and getting paid for physical printed photos straight from your app. You simply need drop the folder into your iPhone or iPad project, and add a photo through our simple SDK interface to send your user to the checkout flow. Our SDK is compatible with all Android versions back to and including SDK code 7.

If you are looking for a lighter SDK that offloads the heavy lifting to a separate app, and sends your users/their photos to this app to make a purchase please check [this Github project](https://github.com/KindredPrints/KindredPrints-Android-Lite).

![Kindred Diagram](https://s3-us-west-1.amazonaws.com/kindredmetaimages/KindredDiagram.png)

## About the Kindred Prints SDK

### What You Get
- Payouts straight to you bank account for every print you sell
- Create custom coupons for your users
- Give specific users credits
- Monitor all orders, users on a custom dashboard
- An photo print cart with complete checkout/payments 
- All related customer service handled by Kindred

### Overview of User Checkout Flow
1. You add photos and (optionally) preregister the user for shipment and order notifications
1. You present the Kindred Prints order flow
1. User sees all photos you added, with list of available products for each photo
1. User selects quantities for each order
1. User adds/selects shipping destination
1. User sees order summary
1. User enters credit card to securely pay for items
1. User receives order confirmation email
1. A day or two later, user receives shipping notification
1. The user receives the prints in the mail

### Current Print Capabilities

- 4" x 4" glossy premium print

    ![4x4 Prints](https://raw.githubusercontent.com/KindredPrints/KindredPrints-iOS/master/Documentation/4by4.jpg)

- 4" x 6" glossy premium print

    ![4x6 Prints](https://raw.githubusercontent.com/KindredPrints/KindredPrints-iOS/master/Documentation/4by6.jpg)

- Two sided vs. one sided

  Our prints can be either one sided or two sided. You can specify the type of print when you send the image request.


## How our cart works

When a user chooses to order a print, that images gets added to the Kindred cart. The user is prompted to choose quantities and can finalize the order with one print. However, if the user chooses to go back, he or she can keep choosing other images to order prints from. Every time he adds images to print, those images are added to the Kindred cart. The images remain in the cart until the user completes the purchase or removes them (quitting the app does not remove the images from the user's cart).

![How it works](https://raw.githubusercontent.com/KindredPrints/KindredPrints-iOS/master/Documentation/OneImage_HowitWorks.png)

### Image types

You can send us user images to be added to the cart from various sources:

- **KPURLImage** is created with a URL to a preview size AND full size, or just a full size.
- **KPMEMImage** is created with a UIImage object in iOS or Bitmap object in android
- **KPLOCImage** is created with a file path string

Aditionally, we can work with you on creating custom images from content your user is creating. For exaple, we are working with a cooking app where we create recipe cards that users can print when they like a particular recipe.

## Android Specific Installation

### Download the SDK with Test Project

Follow the instructions in this section if you want to see how an example test app interfaces with the SDK. All publicly callable functions in the SDK are demonstrated.

#### Option 1: Using git
1. `cd` into your development workspace.
2. Run `git clone git://github.com/kindredprints/kindredprints-android.git` in the command line

#### Option 2: Manual download

1. Download [zipped test project and SDK](https://s3-us-west-1.amazonaws.com/kindredmeta/KindredPrints-Android.zip) to the folder of your choice
2. Unzip the file to your development workspace

#### Afterwards:

1. Open up Eclipse/Android Development Tools

1. Go to **File -> Import** and choose **Existing Android Code into Workspace** under the Android folder

1. Select the **KindredPrints-Android-SDK**

1. Make sure both **KindredPrints-SDK-TestBed** and **KindredPrints-Android-SDK** are selected and click Finish.

1. View **MainActivity.java** in the **KindredPrints-SDK-TestBed** and update this line with your test Kindred App ID (you can grab one by signing up [here](http://sdk.kindredprints.com/signup/).)

    ```java
    private final static String KINDRED_APP_KEY = "YOUR_TEST_KEY_HERE";
    ```
1. Run the project and play with the test app.

1. All the publicly callable SDK functions are demonstrated in **MainActivity.java** 

### Download the raw SDK files

If you would prefer to just add the SDK folder to your project:

1. Grab a zipped copy of the [**KindredPrints-Android-SDK** folder](https://s3-us-west-1.amazonaws.com/kindredmeta/KindredPrints-Android-SDK.zip) or use the **KindredPrints-Android-SDK** folder out of the test project folder (instructions on downloading above)
1. Go to **File -> Import** and choose **Existing Android Code into Workspace** under the Android folder
1. Select the **KindredPrints-Android-SDK**
1. Ensure **KindredPrints-Android-SDK** is selected and click Finish.
1. Right click on your project in Eclipse/Android Development Tools and find **Build Path -> Configure Build Path**
1. Click **Android** on the left
1. In the **Library** subsection on the right, click **Add** and choose **KindredPrints-Android-SDK** from the list

### Using the SDK

#### Testing

All testing should be done using your Kindred test key, obtained through [signing up](http://sdk.kindredprints.com/signup/). With the test key, none of your orders will get sent to the printer, and no credit cards are charged. Be sure to switch this key to the live version before opening it up to the public, or your users' orders won't go through.

With the Kindred test key, you can use a fake credit card with the number *4242 4242 4242 4242* and any valid future date and CVV.

#### Initial project configuration 

##### Permissions
In your project's AndroidManifest.xml, here the recommended permissions that you should request from your app for optimal experience.

- **Internet** (required): We absolutely require access to the internet. Hopefully you already have this permission.
    
    ```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    ```
- **Contacts** (optional): We offer the option for the user to import addresses from their contacts. We don't do anything with the contact permission aside from importing the details of the contact which the user selects. No other data touches our servers.

  ```xml
  <uses-permission android:name="android.permission.READ_CONTACTS"/>
  ```

##### Order Flow Activity
You need to register the order flow activity inside your manifest. Add the line below inside the `<application> </application>` tags.

```xml
<application>
    ....
    <activity android:name="com.kindredprints.android.sdk.KindredOrderFlowActivity" />
</application>
```

#### Example Single Photo Workflow

This type of implementation drives users to checkout based on a single photo. This is primarily useful for apps that deal primarily with editing or improving a single photo.

For this example, we assume that the photo is stored in local memory and you have a URI to reference it. For example using remote URLs, see similar methods in the examples below.

1. Add a button next to the photo that says Print or whatever you feel is appropriate.

1. Add the appropriate imports to your class:

    ```java
    import com.kindredprints.android.sdk.KindredOrderFlow;
    import com.kindredprints.android.sdk.KLOCPhoto;
    import com.kindredprints.android.sdk.KindredOrderFlowActivity;
    ```
1. Create a method to handle the button click and insert this code into it. Make sure update this line with your test Kindred App ID (you can grab one by signing up [here](http://sdk.kindredprints.com/signup/).)

    ```java
    KindredOrderFlow orderFlow  = new KindredOrderFlow(this, "YOUR_TEST_KEY_HERE");
    
    orderFlow.addImageToCart(new KLOCPhoto(null, (String)absoluteFilePathToPicture));

    Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
    startActivityForResult(i, 0);
    ```

1. You're done! Yea - it can really be that simple. We'll take care of the rest.

#### Example Multi Photo Workflow

You would be interested in this example if you are an app that deals with a lot of photos. Let's say you have a whole album of photos, and you want to add 5-10 of them to the photo print cart. Or, you want to give the user the feeling of a cart like experience, and let them individually add photos to the cart before they are ready to "Checkout". You could build a simple "Checkout" button in the top corner of the screen and place an "Add to cart" button next to every photo.

In this example, all photos are located on a remote server, and are passed to the SDK via their URLs. The checkout flow will then cache the photo for display to the user.

1. Add the "Add to cart" button to your project and place it next to each photo in a list

1. Add the appropriate imports to your class:
   ```java
import com.kindredprints.android.sdk.KindredOrderFlow;
import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.KindredOrderFlowActivity;
   ```

1. Create a method to handle the button click and insert this code into it:
   ```java
    KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
    KURLPhoto urlPhoto = new KURLPhoto("1", "http://site.com/img.jpg");
    orderFlow.addImageToCart(urlPhoto);
   ```
Alternatively, if you also store a remote url for a pre rendered preview size image in addition to the original, you can init a KURLPhoto like this.

   ```java
    KURLPhoto urlPhoto = new KURLPhoto("1", "http://site.com/img.jpg", "http://site.com/prevImg.jpg");
   ```
1. Now, add a button somewhere on the display that says "Checkout"

1. Create a method to handle the button click and insert this code into it:
   ```java
    Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
    startActivityForResult(i, 0);
   ```

1. You're done! The user can now check out all of the images they've added before.


### Advanced Functionality

#### Registering a callback

If you want to respond to the user finishing the order flow, override onActivityResult of the class that adds photos to the order flow:
   ```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == KindredOrderFlowActivity.KP_RESULT_CANCELLED) {
        Log.i("MyActivity", "User cancelled Kindred purchase");
      } else if (resultCode == KindredOrderFlowActivity.KP_RESULT_PURCHASED) {
        Log.i("MyActivity", "User completed Kindred purchase!");
      }
    }
   ```

#### Preregistering a user

We require users to enter an email address for a number of reasons, including confirming their order, notifying them when orders are in the mail, and reaching out in case of issues with their order.

However, asking a user to manually enter an email addresses can lower conversion. If you already know the user's email address, you can pass it so they do not need to enter it manually:
```java
KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
orderFlow.preRegisterEmail((String)userEmail);
```

If you know the name of the user as well, we can personalize our emails for a better experience if you call this function.
```java
KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
orderFlow.preRegisterEmail((String)userEmail, (String)name);
```

