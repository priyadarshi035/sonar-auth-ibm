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
package com.jirav.sonarqube.auth.google;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.jirav.sonarqube.auth.google.GoogleSettings.LOGIN_STRATEGY_DEFAULT_VALUE;

public class GoogleIdentityProviderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  Settings settings = new Settings();

  GoogleSettings githubSettings = new GoogleSettings(settings);

  GoogleIdentityProvider underTest = new GoogleIdentityProvider(githubSettings);

  @Test
  public void check_fields() throws Exception {
    assertThat(underTest.getKey()).isEqualTo("google");
    assertThat(underTest.getName()).isEqualTo("Google");
    assertThat(underTest.getDisplay().getIconPath()).isEqualTo("/static/authgoogle/btn_google_light_normal_ios.svg");
    assertThat(underTest.getDisplay().getBackgroundColor()).isEqualTo("#4285F4");
  }

  @Test
  public void is_enabled() throws Exception {
    settings.setProperty("sonar.auth.google.clientId.secured", "id");
    settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);
    settings.setProperty("sonar.auth.google.enabled", true);
    assertThat(underTest.isEnabled()).isTrue();

    settings.setProperty("sonar.auth.google.enabled", false);
    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void init() throws Exception {
    setSettings(true);
    OAuth2IdentityProvider.InitContext context = mock(OAuth2IdentityProvider.InitContext.class);
    when(context.generateCsrfState()).thenReturn("state");
    when(context.getCallbackUrl()).thenReturn("http://localhost/callback");

    underTest.init(context);

    verify(context).redirectTo("https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=id&redirect_uri=http%3A%2F%2Flocalhost%2Fcallback&scope=email%20profile&state=state");
  }

  @Test
  public void fail_to_init_when_disabled() throws Exception {
    setSettings(false);
    OAuth2IdentityProvider.InitContext context = mock(OAuth2IdentityProvider.InitContext.class);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Google Authentication is disabled");
    underTest.init(context);
  }

  private void setSettings(boolean enabled) {
    if (enabled) {
      settings.setProperty("sonar.auth.google.clientId.secured", "id");
      settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
      settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);
      settings.setProperty("sonar.auth.google.enabled", true);
    } else {
      settings.setProperty("sonar.auth.google.enabled", false);
    }
  }
}
