# CONTRIBUTING


## Contents
- [Contributing to the app](#contributing-to-the-app)
- [Contributing to the data](#contributing-to-the-data)


## Contributing to the app
- Do not submit pull requests to update gradle, dependencies or SDK.
- Try not to use any deprecated libraries, dependencies or methods, if other alternatives are available.
- Make sure the characters are properly encoded when translating strings (example: ä as \u00E4, é as \u00E9 etc).
  <br>You can use websites like [Compart](https://www.compart.com/en/unicode), [Symbl](https://symbl.cc/en/unicode/table/) or something else.
  <br>Example: `é` would be shown as `U+00E9` on these websites, so just convert it to `\u00E9` for the android strings.
  <br>The strings can be found here:
  - [English](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values/strings.xml)
  - [Dutch](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values-nl/strings.xml)
  - [French](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values-fr/strings.xml)
  - [German](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values-de/strings.xml)
  - [Italian](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values-it/strings.xml)
  - [Spanish](https://github.com/techlore/Plexus-app/blob/main/app/src/main/res/values-es/strings.xml)


## Contributing to the data
This has been integrated in the app. Please **do not** open pull requests to add new apps as they won't be accepted.
Refer to the [wiki](https://github.com/techlore/Plexus-app/wiki/Help#apps-submission-procedure) to see how to submit data for apps.