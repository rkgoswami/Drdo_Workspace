# Drdo_Workspace
This is repository to backUp works related to DRDO project.

# Step to be followed to use this app:
  - Install the app using app-debug.apk file present in */OtpEncryption/app/build/outputs/apk/* location (*may require enabling the setting to install the app from an unknown source*), or download the project and import it in android studio and run.
  - Go to setting to give permission of sending msg for this app (*by default latest version of android blocks every permission*).
  - Guideline to use the app:
      - Firstly press generate RSA key button to generate public and private keys. You can scroll the displayed RSA keys to see the complete key.
      - Press Generate OTP Button to generate OTP using HMAC algorithm and the current timestamp.
      - Press encrypt button to encrypt the generated OTP using public key
      - Enter your mobile number to send the encrypted OTP to yourself for demo purpose (*SMS charges may apply*).
      - Once you receive an encrypted OTP, copy it and paste in the popup dialog box asking for OTP.
      - Then press the validate button to decrypt the OTP and display the decrypted OTP.
  - **Note**: I have not yet added the Hummingbird algorithm because some issues with using the native library in android app. 
