# E-Voting_Android
This is the project for the E-Voting Android application.

## Installation
1. Install Android Studio from [this site](https://developer.android.com/studio?hl=zh-tw).
2. Clone the repository using the following commands in CLI:
```bash
git clone https://github.com/penny11124/E-Voting_Android.git
```

## Usage (For physical Android devices)
1. Connect the Android device to the computer with Android Studio.
2. Enable the **Developer Mode** and **USB Debugging** options if not yet enabled.
3. Open **UrekaApp** directory using Android Studio.
4. Select the connecting device in the **Available Devices** options in the upper-right corner.
5. Press the **Run 'app'** button in the upper-right corner.
> The first time pressing the button would take more time since the project needs to be built.

## Operation flow
### 1. Ownership Transfer
1. Execute the app on the Admin Agent.
2. Press the **Admin Agent** button.
3. Press the **Manufacturer: Connect to VM** button to make the Manufacturer connect to Voting Machine.
4. Press the **Manufacturer: Init device** button to make the Manufacturer send an Init UTicket to Voting Machine.
5. Press the **Manufacturer: Issue Ownership UTicket** button to make the Manufacturer issue an Ownership UTicket to Admin Agent.
6. Press the **Disconnect** button to disconnect between Manufacturer and Voting Machine.
7. Press the **Admin Agent: Connect to VM** button to make the Admin Agent connect to Voting Machine.
8. Press the **&Admin Agent: Apply Ownership UTicket** button to make Admin Agent apply the Ownership UTicket.
> Manufacturer and Admin Agent is implemented on the same device.
> 
> The second text layout on the screen shows the connection status of the device. The same layout also exists in the UI of Voter Agent.

### 2. Voting Configuration
1. Press the **Get Data** button to get the data of the voting, i.e. the candidates and valid voters.
2. Press the **Admin Agent: Apply Config UTicket** button to make Admin Agent send a Config UTicket to Voting Machine.
> This step may take more time since the data is separated into several segments and transferred.

### 3. Voter Voting
1. Press the **Admin Agent: Advertise for Voter** button to make Voter Agent can discover Admin Agent.
2. Execute the app on Voter Agent and choose a Voter.
3. Press the **Connect To Admin Agent** button on Voter Agent to make Voter Agent connect to Admin Agent.
4. Press the **Request UTicket** button on Voter Agent to make Voter Agent request a Voting UTicket from Admin Agent.
5. Press the **Disconnect** button on **both devices** to make them disconnect.
6. Press the **Connect To Voting Machine** button to make Voter Agent connect to Voting Machine.
7. Press the **Apply UTicket** button to make Voter Agent apply the Voting UTicket to Voting Machine.
8. After the voting UI appears, vote a candidate and press the **Confirm** button.
9. Wait for the rest of the data transfer to be completed.

### 4. Return Voting RTicket to Admin Agent
1. Press the **Disconnect** button on Voter Agent to make Voter Agent and Voting Machine disconnect.
2. Connect Admin Agent and Voter Agent. The steps are the same as Step 3.1~3.3.
3. Press the **Send RTicket to Admin Agent** button on Voter Agent to make Voter Agent send the Voting RTicket to Admin Agent.
4. Press the **Show RTicket** button on the Voter Agent to see the Voting RTicket. (Optional)

### 5. Voting tally
1. Connect Admin Agent to Voting Machine by pressing **Disconnect** button on Voter Agent and **Admin Agent: Connect to VM** button on Admin Agent.
2. Press the **Admin Agent: Apply Tally UTicket** button on Admin Agent to make Admin Agent send a Tally UTicket to Voting Machine.
> This step may take more time since the data is separated into several segments and transferred.
3. Press the **Show RTickets** to see the results of the candidates and the Voting RTickets of the voters. (Optional)
