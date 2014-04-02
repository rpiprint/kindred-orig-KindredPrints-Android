# Kindred Prints Android SDK

The Kindred Prints SDK makes it extremely easy to start selling and getting paid for physical printed photos straight from your app. You simply need drop the folder into your iPhone or iPad project, and add a photo through our simple SDK interface to send your user to the checkout flow.
`small screens of sdk here`

## About the Kindred Prints SDK

Here are the details of what is included in the Kindred Printing platform

### What You Get
- Payouts straight to you bank account for every print you sell
- Create custom coupons for your users
- Give specific users credits
- Monitor all orders, users on a custom dashboard
- An photo print cart with complete checkout/payments 
- All related customer service handled by Kindred

### Overview of User Checkout Flow
1. [behind the scenes] You add photos and (optionally) preregister the user for shipment, order notifications
1. Present the Kindred Prints order flow
1. User sees all photos you added, with list of available products for each photo
1. User selects quantities for each order
1. User adds/selects shipping destination
1. User sees order summary
1. User enters credit card to (securely) pay for items
1. User receives order confirmation email
1. A day or two later, user receives shipping notification
1. The user receives the prints in the mail

### Current Print Capabilities

- 4" x 4" glossy premium print
...`put picture here`
- 4" x 6" glossy premium print
...`put picture here`

## Android Specific Installation

The entire SDK is open sourced and a release schedule will be publicized soon. You can download the raw SDK files or clone the entire project with accompanying test app.

### Download the SDK with Test Project

Follow the instructions in this section if you want to see how an example test app interfaces with the SDK. All publicly callable functions in the SDK are demonstrated.

You can grab a zipped copy here.

1. Dowload the [zipped test project and SDK](https://s3-us-west-1.amazonaws.com/kindredmeta/KindredPrints-Android.zip) to the folder of your choice

1. Unzip it to your development workspace

OR clone this project and open it in Xcode.

1. CD into your development directory.

1. Run `git clone git://github.com/kindredprints/kindredprints-android.git` in the command line

Then,

3. Open up Eclipse/Android Development Tools

3. Go to **File -> Import** and choose **Existing Android Code into Workspace** under the Android folder

3. Select the **KindredPrints-Android-SDK**

3. Make sure both **KindredPrints-SDK-TestBed** and **KindredPrints-Android-SDK** are selected and click Finish.

3. View **MainActivity.java** in the **KindredPrints-SDK-TestBed** and update this line with your test Kindred App ID (you can grab one by signing up [here](http://sdk.kindredprints.com/signup/).)

```java
private final static String KINDRED_APP_KEY = "YOUR TEST KEY HERE";
```
3. Run the project and play with the test app.

3. All the publicly callable SDK functions are demonstrated in **MainActivity.java** 

### Download the raw SDK files

If you would prefer to just add the SDK folder to your project and get started right away with out looking at the test app, follow these instructions.

1. You can grab a zipped copy of the ***KindredPrints-Android-SDK** folder [here](https://s3-us-west-1.amazonaws.com/kindredmeta/KindredPrints-Android-SDK.zip)

Or use the **KindredPrints-Android-SDK** folder out of the test project folder (instructions on downloading above)

1. Go to **File -> Import** and choose **Existing Android Code into Workspace** under the Android folder
1. Select the **KindredPrints-Android-SDK**
1. Ensure **KindredPrints-Android-SDK** is selected and click Finish.
1. Right click on your project in Eclipse/Android Development Tools and find **Build Path -> Configure Build Path**
1. Click **Android** on the left
1. In the **Library** subsection on the right, click **Add** and choose **KindredPrints-Android-SDK** from the list

### Using the SDK

#### Initial project configuration 

In order to fully employ all of the SDK functions, here are the initial project configurations necessary. Our SDK is compatible with all Android versions back to and including SDK code 7.

**Permissions**
In your project's AndroidManifest.xml, here the recommended permissions that you should request from your app for optimal experience.

Internet [required]: We absolutely require access to the internet. Hopefully you already have this permission.
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

External storage [required]: We do a lot of file caching with images due to the extreme memory constraints of the Android ecosystem. At any given time, our app should not required more than 18 mb of additional run time memory.
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

Contacts [Optional]: We offer the option for the user to import addresses from their contacts. We don't do anything with the contact permission aside from importing the details of the contact which the user selects. No other data touches our servers.
```xml
<uses-permission android:name="android.permission.READ_CONTACTS"/>
```

**Order Flow Activity**
You need to register our order flow activity inside your manifest. Add the line below inside the <application> </application> tags.
```xml
<application>
    ....
    <activity android:name="com.kindred.kindredprints_android_sdk.KindredOrderFlowActivity" />
</application>
```
#### A note about testing

All testing should be done using the test key you get through signing up (sign up located here: 'link to partner signup'), which means that none of your orders get sent to the printer and no credit cards are charged. Be sure to switch this key to the live version before opening it up to the public.

With the Kindred test key, you can use a fake credit card with the number **4242 4242 4242 4242** and any valid future date/cvv code.

#### Example Single Photo Workflow

Everyone should read this example implementation, as it shows more details than the multi photo flow (pre user registration and delegate callbacks). This is the type of implementation if you would like to drive users to checkout based on a single photo. You would be interested in this if you are an app that deals primarily with editing or improving a single photo or you've developed a beautiful layout of user content that you would like to let your users buy (like a cooking recipe card).

For this example, we assume that the photo is stored in local memory and you have a URI to reference it. For example using remote URLs, see similar methods in the examples below.

5. Add a button next to the photo that says Print or whatever you feel is appropriate.

5. Add the appropriate imports to your class:
   ```java
import com.kindred.kindredprints_android_sdk.KindredOrderFlow;
import com.kindred.kindredprints_android_sdk.KLOCPhoto;
import com.kindred.kindredprints_android_sdk.KindredOrderFlowActivity;
   ```
5. Create a method to handle the button click and insert this code into it. Make sure update this line with your test Kindred App ID (you can grab one by signing up [here](http://sdk.kindredprints.com/signup/).)
   ```java
    KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
    
    orderFlow.addImageToCart(new KLOCPhoto(null, (String)absoluteFilePathToPicture));


    Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
    startActivityForResult(i, 0);
   ```
5. Replace the "KINDRED_APP_KEY" with your test or live key (depending on which mode you are in). You can get one for you app through the quick signup process [here](http://sdk.kindredprints.com/signup/)

5. You're done! Yea - it can really be that simple. We'll take care of the rest.

**Callback Addendum:**
 If you want to register for a callback when the SDK finishes, override onActivityResult. To do this, follow these instructions:
6. Override onActivityResult and look for our static result codes. An example can be found in our TestProject like so.
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
6. Fill in the appropriate handling of result cancelled and purchased.
6. Done!

**User Preregistration Addendum:** 
We require users to enter an email address for a number of reasons. The two most common cases are for the order confirmation and shipping notifications that we send when the prints are mailed. We also need to reach out to the customer in case of any issues with their orders. 

We understand that email addresses can be a friction point that lowers conversion, so we wanted to let you take care of this if you already know the email address of the user. To pre register an email address with the service, it's very simple, just call these lines of code anywhere.
   ```java
KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
orderFlow.preRegisterEmail((String)userEmail);
   ```
If you already initialized the **KindredOrderFlow** class, you can just call 'preRegisterEmail' on it.

Also of note, if you know the name of the user as well, we can personalize the emails for a better experience if you call this function
   ```java
KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
orderFlow.preRegisterEmail((String)userEmail, (String)name);
   ```

#### Example Multi Photo Workflow

You would be interested in this example if you are an app that deals with a lot of photos. Let's say you have a whole album of photos, and you want to add 5-10 of them to the photo print cart. Or, you want to give the user the feeling of a cart like experience, and let them individually add photos to the cart before they are ready to "Checkout". You could build a simple "Checkout" button in the top corner of the screen and place an "Add to cart" button next to every photo.

In this example, all photos are located on a remote server, and are passed to the SDK via their urls. The checkout flow will then cache the photo for display to the user.

7. Add the "Add to cart" button to your project and place it next to each photo in a list

7. Add the appropriate imports to your class:
   ```java
import com.kindred.kindredprints_android_sdk.KindredOrderFlow;
import com.kindred.kindredprints_android_sdk.KURLPhoto;
import com.kindred.kindredprints_android_sdk.KindredOrderFlowActivity;
   ```

7. Create a method to handle the button click and insert this code into it:
   ```java
    KindredOrderFlow orderFlow  = new KindredOrderFlow(this, KINDRED_APP_KEY);
    KURLPhoto urlPhoto = new KURLPhoto("1", "http://site.com/img.jpg");
    orderFlow.addImageToCart(urlPhoto);
   ```
Alternatively, if you also store a remote url for a pre rendered preview size image in addition to the original, you can init a KURLPhoto like this.

   ```java
    KURLPhoto urlPhoto = new KURLPhoto("1", "http://site.com/img.jpg", "http://site.com/prevImg.jpg");
   ```
7. Now, add a button somewhere on the display that says "Checkout"

7. Create a method to handle the button click and insert this code into it:
   ```java
    Intent i = new Intent(getApplicationContext(), KindredOrderFlowActivity.class);
    startActivityForResult(i, 0);
   ```

7. You're done! The user can now check out all of the images they've added before.
