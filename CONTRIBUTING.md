# CONTRIBUTING

**Plexus is only for applications directly or indirectly from the Google Play Store. Please do not submit applications from F-Droid as they are all expected to work.**



## Contributing to the data

### General format
Ensure you're adding new applications in alphabetical order to make merging as simple as possible.

The general format to add new applications is as follows:

```json
{
  "Application": "Application name",
  "Package": "Package name of the application",
  "Version": "Version of the application",
  "DG_Rating": "Degoogled rating for the application",
  "MG_Rating": "MicroG rating for the application",
  "DG_Notes": "Degoogled notes for the application",
  "MG_Notes": "MicroG notes for the application"
}
```

### Testing Standards
- Test only Google Play Store applications _(Regardless of where they came from, including the Aurora Store)_
- Kindly disable Netguard and other blockers
- No Google Play Services. Plexus evaluates MicroG, or nothing. Plexus assumes all variants of Google Play Services (ex. Sandboxed Play Services, OpenGApps, etc.) will function without issues for compatibility.
- Pay extra attention to notifications as some will not properly work. If you're testing on MicroG, it is assumed you have enabled GCM/FCM. If you have not enabled GCM/FCM, you are still free to report applications, but please exclude any information regarding missing notifications. (Notifications working is okay and encouraged even with GCM/FCM disabled.)
- If you're testing **with MicroG** only, input data in the MG_Rating & MG_Notes and enter DG_Rating and DG_Notes as "X".
- If you're testing with a fully de-googled ROM only **without MicroG**, input data in the DG_Rating & DG_Notes and enter MG_Rating and MG_Notes as "X".
- We check all data for accuracy as best we can, and ultimately hold the right to turn down a submission if we feel it wasn't tested properly. Generally, we will follow up with individuals for clarification when necessary.

### Example
Assuming Twitter as the app for example here.

- Tested on both Degoogled ROM and MicroG:
```json
{
  "Application": "Twitter",
  "Package": "com.twitter.android",
  "Version": "9.41.0",
  "DG_Rating": "3",
  "MG_Rating": "3",
  "DG_Notes": "No notifications",
  "MG_Notes": "No notifications"
}
```

- Tested on Degoogled ROM only, without MicroG:
```json
{
  "Application": "Twitter",
  "Package": "com.twitter.android",
  "Version": "9.41.0",
  "DG_Rating": "3",
  "MG_Rating": "X",
  "DG_Notes": "No notifications",
  "MG_Notes": "X"
}
```

- Tested on MicroG only:
```json
{
  "Application": "Twitter",
  "Package": "com.twitter.android",
  "Version": "9.41.0",
  "DG_Rating": "X",
  "MG_Rating": "3",
  "DG_Notes": "X",
  "MG_Notes": "No notifications"
}
```



## Contributing to the app
- Try to stick to the project's existing code style and naming conventions.
- Make sure not to use any deprecated libraries, dependencies or methods. 
