<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="90"/>

# Plexus-app
***Remove the fear of Android app compatibility on de-Googled devices.***

Android app for [Plexus](https://plexus.techlore.tech)

<img src="https://img.shields.io/f-droid/v/tech.techlore.plexus?color=green&style=for-the-badge" alt="F-Droid Version"> <img src="https://img.shields.io/github/v/release/techlore/Plexus-app?color=212121&label=GitHub&style=for-the-badge" alt="GitHub Version"> <img src="https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/tech.techlore.plexus?&style=for-the-badge" alt="IzzyOnDroid Version">



## Important updates as of May 12, 2022
Hey everyone, thanks for visiting the repo & thank you to all contributors. Plexus is going through a massive transition right now:
* [The Plexus application has been released](https://github.com/techlore/Plexus-app)
* [The Plexus data in that repo is now in JSON format](https://github.com/techlore/Plexus-app/blob/main/Plexus.json) meaning we now have two different datasets, at least temporarily. (The website has its own data as well)
* The JSON format is already better than the terrible CSV format in the website repo, but the JSON format is actually temporary. We are working on an API to avoid the entire GitHub submission workflow.
* Because so many changes are happening at once, we will not be accepting pull requests until our new workflow is completed. Once it's completed, we will be manually porting over your submissions to the new data and closing the PRs.
* Once everything is completed, all submissions will only be done via the application. We don't have an estimate yet, but it'll be the best ever experience once this is completed, enabling anyone to easily submit apps in an automated fashion in bulk.

## Contents
- [Explanation](#explanation)
- [What Do The Ratings Mean?](#what-do-the-ratings-mean)
- [Screenshots](#screenshots)
- [Download](#download)
- [Changelog](#changelog)
- [Privacy Policy](#privacy-policy)
- [Issues](#issues)
- [Contributing](#contributing)
- [Libraries Used](#libraries-used)
- [Credits](#credits)
- [License](#license)




## Explanation
Google Play Services are an integral part of most Android devices that enable users to utilize their Google account on their phone, as well as enable Google-specific features for applications that rely on them.

When users move to a de-googled ROM like GrapheneOS, CalyxOS or LineageOS, they are faced with opening their standard applications hoping they work. Plexus aims to beat the guessing game and allow users to know exactly what will happen once they flash a new ROM. Plexus supports ROMs with no Google Play Services e.g: [(GrapheneOS)](https://grapheneos.org/), as well as ROMs with [microG](https://microg.org/), an open source alternative of Google Play Services, e.g: [(CalyxOS)](https://calyxos.org/).



## What Do The Ratings Mean?
- X: App has not been tested
- 1: Unusable, mostly apps that fail to open
- 2: Acceptable but with missing or broken functionality
- 3: Almost everything works with minimal caveats
- 4: Perfect or like-perfect experience



## Screenshots
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/>



## Download
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" 
      alt='Get it on F-Droid' 
      height="80">](https://f-droid.org/packages/tech.techlore.plexus)
[<img src="https://camo.githubusercontent.com/70bffd8873ab81e1bb0bccc44e488c3a989e3bd5/68747470733a2f2f692e6962622e636f2f71306d6463345a2f6765742d69742d6f6e2d6769746875622e706e67"
     alt="Get it on GitHub"
     height="80">](https://github.com/techlore/Plexus-app/releases/latest)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" 
      alt='Get it on IzzyOnDroid' 
      height="80">](https://apt.izzysoft.de/fdroid/index/apk/tech.techlore.plexus)



## Changelog
All notable changes to this project will be documented in the [changelog](https://github.com/techlore/Plexus-app/blob/main/CHANGELOG.md).



## Privacy Policy
Privacy policy is located [here](https://github.com/techlore/Plexus-app/blob/main/PRIVACY.md).



## Issues
If you find bugs or have suggestions, please report it to the [issue tracker](https://github.com/techlore/Plexus-app/issues). We encourage you to search for existing issues before opening a new one. Any duplicates will be closed immediately.



## Contributing
Please read the [contributing guidelines](https://github.com/techlore/Plexus-app/blob/main/CONTRIBUTING.md) before contributing.

- The plexus data can be found [here](https://github.com/techlore/Plexus-app/blob/main/Plexus.json).
- New pull requests can be submitted [here](https://github.com/techlore/Plexus-app/pulls).
- The application is currently missing one **major** feature: submissions directly from the app, enabling users to automatically submit their applications. If you believe you are able to help us develop a workflow to easily track and maintain a database of user submissions, please reach out to us at contact@techlore.tech - thanks!



## Libraries Used
- [Jackson](https://github.com/FasterXML/jackson) | [Apache License 2.0](https://github.com/FasterXML/jackson-core/blob/2.14/LICENSE)
- [OkHttp](https://github.com/square/okhttp) | [Apache License 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt)
- [AndroidFastScroll](https://github.com/zhanghai/AndroidFastScroll) | [Apache License 2.0](https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE)



## Credits
- [the-weird-aquarian](https://github.com/the-weird-aquarian) For building this application and taking Plexus a major step forward towards full automation!
- [henryistaken](https://github.com/henryistaken) for going forward with the project, keeping up with and replying to my spams.
- [parveshnarwal](https://github.com/parveshnarwal) for helping with the initial implementation of json into this app.
- [Tom Konidas](https://github.com/tomkonidas) For building the original Plexus website and speeding up the development of this project.
- Some additional icons (other than the official ones by Google) are provided by [MaterialDesignIcons](https://github.com/Templarian/MaterialDesign) | [Pictogrammers Free License](https://github.com/Templarian/MaterialDesign/blob/master/LICENSE)



## License
Except where indicated otherwise, this project is licensed under the terms of [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).
