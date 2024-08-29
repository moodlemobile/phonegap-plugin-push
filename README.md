# Cordova Plugin Push (fork)

This is a fork of `cordova-plugin-push` by [Moodle HQ](https://moodle.com/). If you are looking for the documentation, you can read the original at [havesource/cordova-plugin-push](https://github.com/havesource/cordova-plugin-push).

## Modifications from the original

We created this fork because we needed to include the following modifications in [our mobile application](https://github.com/moodlehq/moodleapp):

| PR | Description |
| -- | ----------- |
| [phonegap-plugin-push#2581](https://github.com/phonegap/phonegap-plugin-push/pull/2581) | Add compatibility with katzer/cordova-plugin-local-notifications 0.9.0-beta.3 |
| [phonegap-plugin-push#2654](https://github.com/phonegap/phonegap-plugin-push/pull/2654) | on notification ios does not work on cold start |
| [phonegap-plugin-push#2696](https://github.com/phonegap/phonegap-plugin-push/pull/2696) | Allow sorting inbox notifications in ascending order |
| [phonegap-plugin-push#2705](https://github.com/phonegap/phonegap-plugin-push/pull/2705) | Clear inbox messages list when clicking action button |
| [phonegap-plugin-push#2743](https://github.com/phonegap/phonegap-plugin-push/pull/2743) | Support messaging style for notifications |
| [havesource#183](https://github.com/havesource/cordova-plugin-push/pull/183) | Claim PushHandlerActivity permissions |
| - | Fix push notifications in Android 4 and 5 |
| - | Support Android X |
| - | Support sender image |
| - | Claim BackgroundHandlerActivity permissions |
| - | Add android:exported to services |
| - | Fix crash if no valid configuration provided |
| - | Support encrypted push notifications (Android 8+ and iOS 13+) |
| - | Update default version of Firebase for iOS |
| - | Fix obtaining FCM token in iOS when using Firebase 10.23 |

This fork is based of `v4.0.0` and it also includes some commits that hadn't been released yet.

You can see all the changes here: [2.0.0...moodlemobile:v4.0.0-moodle.10](https://github.com/havesource/cordova-plugin-push/compare/2.0.0...moodlemobile:v4.0.0-moodle.10)

## Installation

You can install this package using the [original installation instructions](https://github.com/havesource/cordova-plugin-push/blob/master/docs/INSTALLATION.md), but installing this package instead:

```sh
cordova plugin add @moodlehq/phonegap-plugin-push@4.0.0-moodle.10
```
