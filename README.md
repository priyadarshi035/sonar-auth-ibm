# sonar-auth-IBM
External authentication provider for Sonarqube using IBM OAuth2 / OpenID Connect

This project is a port of the existing project [sonar-auth-github](https://github.com/SonarSource/sonar-auth-github).

It maintains the same project structure, and I attempted to make only those changes necessary.

The directory `diffs` contains the file-by-file diffs between `sonar-auth-github` and `sonar-auth-ibm` as of Feb 8, 2017. These diffs won't be updated, and are intended to provide historical context as well as attribution to the original authors.

## What is different

In terms of technology stack, the main change is an updated version of [scribe-java](https://github.com/scribejava/scribejava), from 2.1.0 to 2.3.0.

In terms of functional differences, there are two changes:

1. the `loginStrategy` option value of `Unique` is not supported
2. the default value for option `allowUsersToSignUp` is `false`

#### Details for item 1

The GitHub plugin has a `loginStrategy` option setting of `Unique` which will generate a unique value for the Sonar user login ID iff user creation is allowed (option `sonar.auth.github.allowUsersToSignUp=true`) and the user is not already provisioned in Sonarqube.

Since IBM authentication is (globally) authenticating a user's email address, this address is by definintion unique. This also means that in order for a user to be able to authenticate with IBM, their login ID must be set to their IBM email address. This can be safely updated for any existing Sonarqube users for whom you wish to enable IBM authentication.

#### Details for item 2

The other functional change is that the default value for option `sonar.auth.ibm.allowUsersToSignUp` is `false`. If you want anyone with a IBM account to be able to automatically create an account on your Sonarqube system you should set this to `true`. Note that with the default setting, you must explicitly create a user account in Sonarqube with the `Login` value set to the email address used for IBM authentication. When creating an account you must also set the `Name` of the user and create a password. If you only want this user to be able to authenticate with IBM, create a highly complex random password and don't record it anywhere. Also note that when logging in with IBM, the `Name` field will be updated with the name provided by IBM authentication each time the user is authenticated. If the user updates their name in their IBM account, this change will be reflected in Sonar.

## OAuth Configuration

When configuring the Sonarqube plugin you must supply the OAuth client ID and client secret using options `sonar.auth.ibm.clientId` and `sonar.auth.ibm.clientSecret`.

Restricting authentication only to members of your IBM for Work domain(s) would allow the `sonar.auth.ibm.allowUsersToSignUp` option to be more safely enabled.
