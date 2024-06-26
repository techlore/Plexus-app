<img src="fastlane/metadata/android/en-US/images/icon.png" width="85"/>

# Plexus-app
***Remove the fear of Android app compatibility on de-Googled devices.***

Android app for [Plexus](https://plexus.techlore.tech)

<img src="https://img.shields.io/f-droid/v/tech.techlore.plexus?logo=FDroid&color=green&style=for-the-badge" alt="F-Droid Version"> <img src="https://img.shields.io/github/v/release/techlore/Plexus-app?logo=GitHub&label=GitHub&color=212121&style=for-the-badge" alt="GitHub Version">


## Contents
- [Wiki](#wiki)
- [Screenshots](#screenshots)
- [Download](#download)
- [Changelog](#changelog)
- [Privacy policy](#privacy-policy)
- [Issues](#issues)
- [Contributing](#contributing)
- [Credits](#credits)
- [License](#license)


## Wiki
To learn more about what Plexus is and what it does, please visit the [wiki](https://github.com/techlore/Plexus-app/wiki).


## Screenshots
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/>


## Download
Reproducible builds are enabled, allowing you to install and upgrade the app from any of the following sources:

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt='Get it on F-Droid'
height="80">](https://f-droid.org/packages/tech.techlore.plexus)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
alt="Get it on IzzyOnDroid"
height="80">](https://apt.izzysoft.de/fdroid/index/apk/tech.techlore.plexus?repo=main)
[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png"
alt="Get it on GitHub"
height="80">](https://github.com/techlore/Plexus-app/releases/latest)

### Verify integrity if downloaded from GitHub
To verify the integrity of the `.apk`/`.aab` files, if downloaded from GitHub, perform the following steps:

<details>
  <summary><b>Windows</b></summary>

1. Open Powershell by searching for it in the `Start menu` OR by pressing `Win + r` and typing `powershell`
2. Change directory to the downloaded path
   ```
   cd "C:\path\to\downloaded\file"
   ```
   Example:
   ```
   cd "C:\Users\JohnDoe\Downloads"
   ```
3. Compute the SHA-256 Hash
   ```
   Get-FileHash -Algorithm SHA256 -Path "filename"
   ```
   Example:
   ```
   Get-FileHash -Algorithm SHA256 -Path "Plexus_v2.0.0.apk"
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>

<details>
  <summary><b>Linux & macOS</b></summary>

1. Open terminal
2. Change directory to the downloaded path
   ```
   cd /path/to/downloaded/file
   ```
   Example:
   ```
   cd /home/JohnDoe/Downloads/
   ```
3. Compute the SHA-256 Hash
   ```
   sha256sum filename
   ```
   Example:
   ```
   sha256sum Plexus_v2.0.0.apk
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>


## Changelog
All notable changes to this project will be documented in the [changelog](https://github.com/techlore/Plexus-app/blob/main/CHANGELOG.md).


## Privacy policy
Privacy policy is located [here](https://github.com/techlore/Plexus-app/blob/main/PRIVACY.md).


## Issues
If you find bugs or have suggestions, please report it to the [issue tracker](https://github.com/techlore/Plexus-app/issues). We encourage you to search for existing issues before opening a new one. Any duplicates will be closed immediately.


## Contributing
Please read the [contributing guidelines](https://github.com/techlore/Plexus-app/blob/main/CONTRIBUTING.md) before contributing.

New pull requests can be submitted [here](https://github.com/techlore/Plexus-app/pulls).


## Credits
- [StellarSand](https://github.com/StellarSand) for developing this app.
- [parveshnarwal](https://github.com/parveshnarwal) for helping with the initial implementation of json into this app.
- [tomkonidas](https://github.com/tomkonidas) for developing the API & the Plexus website which helped in the development of this project.
- [Henry](https://github.com/henry-fisher) for going forward with the project and designing/modifying some of the icons.
- [Contributors](https://github.com/techlore/plexus-app/graphs/contributors) for making this app better.
- Everyone who has and continues to contribute data to make this project better than ever.


## License
This project is licensed under the terms of [GPL v3.0 license](https://github.com/techlore/Plexus-app/blob/main/LICENSE).