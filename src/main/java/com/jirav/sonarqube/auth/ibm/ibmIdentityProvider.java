/*
 * Google Authentication for SonarQube
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jirav.sonarqube.auth.ibm;

import com.github.scribejava.apis.GoogleApi20;                                //check for this library
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuth20Service;
import javax.servlet.http.HttpServletRequest;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.lang.String.format;
import static com.jirav.sonarqube.auth.ibm.ibmSettings.LOGIN_STRATEGY_PROVIDER_ID;

@ServerSide
public class ibmIdentityProvider implements OAuth2IdentityProvider {

  private static final Logger LOGGER = Loggers.get(ibmIdentityProvider.class);

  private final ibmSettings settings;

  public ibmIdentityProvider(ibmSettings settings) {
    this.settings = settings;
  }

  @Override
  public String getKey() {
    return "ibm";
  }

  @Override
  public String getName() {
    return "IBM";
  }

  @Override
  public Display getDisplay() {
    return Display.builder()
      // URL of src/main/resources/static/btn_google_light_normal_ios.svg at runtime
      .setIconPath("/static/authgoogle/btn_google_light_normal_ios.svg")
      .setBackgroundColor("#4285F4")
      .build();
  }

  @Override
  public boolean isEnabled() {
    return settings.isEnabled();
  }

  @Override
  public boolean allowsUsersToSignUp() {
    return settings.allowUsersToSignUp();
  }

  @Override
  public void init(InitContext context) {
    String state = context.generateCsrfState();
    OAuth20Service scribe = prepareScribe(context)
      .scope("email profile")
      .state(state)
      .build(GoogleApi20.instance());            //check this api
    String url = scribe.getAuthorizationUrl();
    context.redirectTo(url);
  }

  @Override
  public void callback(CallbackContext context) {
    context.verifyCsrfState();

    HttpServletRequest request = context.getRequest();
    OAuth20Service scribe = prepareScribe(context).build(GoogleApi20.instance());           //check this api
    String oAuthVerifier = request.getParameter("code");
    OAuth2AccessToken accessToken = scribe.getAccessToken(new Verifier(oAuthVerifier));

    OAuthRequest userRequest = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v2/userinfo", scribe);     //check this api
    scribe.signRequest(accessToken, userRequest);

    com.github.scribejava.core.model.Response userResponse = userRequest.send();
    if (!userResponse.isSuccessful()) {
      throw new IllegalStateException(format("Fail to authenticate the user. Error code is %s, Body of the response is %s",
        userResponse.getCode(), userResponse.getBody()));
    }
    String userResponseBody = userResponse.getBody();
    LOGGER.trace("User response received : %s", userResponseBody);
    GsonUser gsonUser = GsonUser.parse(userResponseBody);

    UserIdentity userIdentity = UserIdentity.builder()
      .setProviderLogin(gsonUser.getEmail())
      .setLogin(gsonUser.getEmail())
      .setName(gsonUser.getName())
      .setEmail(gsonUser.getEmail())
      .build();
    context.authenticate(userIdentity);
    context.redirectToRequestedPage();
  }

  private ServiceBuilder prepareScribe(OAuth2IdentityProvider.OAuth2Context context) {
    if (!isEnabled()) {
      throw new IllegalStateException("IBM Authentication is disabled");
    }
   return new ServiceBuilder()
     .apiKey(settings.clientId())
     .apiSecret(settings.clientSecret())
     .callback(context.getCallbackUrl());
  }
}
