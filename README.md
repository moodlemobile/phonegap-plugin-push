# phonegap-plugin-push (fork)

This is a fork of `phonegap-plugin-push` by [Moodle HQ](https://moodle.com/). If you are looking for the documentation, you can read the original at [phonegap/phonegap-plugin-push](https://github.com/phonegap/phonegap-plugin-push).

## Modifications from the original

We created this fork because we needed to include the following modifications in [our mobile application](https://github.com/moodlehq/moodleapp):

| PR | Description |
| -- | ----------- |
| [#2581](https://github.com/phonegap/phonegap-plugin-push/pull/2581) | Add compatibility with katzer/cordova-plugin-local-notifications 0.9.0-beta.3 |
| [#2654](https://github.com/phonegap/phonegap-plugin-push/pull/2654) | on notification ios does not work on cold start |
| [#2696](https://github.com/phonegap/phonegap-plugin-push/pull/2696) | Allow sorting inbox notifications in ascending order |
| [#2705](https://github.com/phonegap/phonegap-plugin-push/pull/2705) | Clear inbox messages list when clicking action button |
| [#2743](https://github.com/phonegap/phonegap-plugin-push/pull/2743) | Support messaging style for notifications |
| - | Fix push notifications in Android 4 and 5 |

You can see all the changes here: [2.2.3...moodlemobile:v2.2.3-moodle.1](https://github.com/phonegap/phonegap-plugin-push/compare/2.2.3...moodlemobile:v2.2.3-moodle.1)

## Installation

You can install this package using the [original installation instructions](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/INSTALLATION.md), but installing this package instead:

```sh
cordova plugin add @moodlehq/phonegap-plugin-push@2.2.3-moodle.1
```
